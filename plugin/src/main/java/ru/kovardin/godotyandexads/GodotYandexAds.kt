// TODO: Update to match your plugin's package name.
package ru.kovardin.godotyandexads

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.ArraySet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.Toast
import org.godotengine.godot.Dictionary
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.instream.MobileInstreamAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader

class GodotYandexAds(godot: Godot) : GodotPlugin(godot) {
    val tag = "GodotYandexAds"

    private lateinit var layout: FrameLayout
    private var layoutParams: FrameLayout.LayoutParams? = null

    private var interstitials: MutableMap<String, InterstitialAd> = mutableMapOf()
    private var rewardeds: MutableMap<String, RewardedAd> = mutableMapOf()
    private var banners: MutableMap<String, BannerAdView> = mutableMapOf()

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    override fun getPluginSignals(): Set<SignalInfo> {
        val signals: MutableSet<SignalInfo> = ArraySet()
        signals.add(SignalInfo("ads_initialized"))
        // banner
        signals.add(SignalInfo("banner_loaded", String::class.java))
        signals.add(SignalInfo("banner_failed_to_load", String::class.java, Integer::class.java))
        signals.add(SignalInfo("banner_ad_clicked", String::class.java))
        signals.add(SignalInfo("banner_left_application", String::class.java))
        signals.add(SignalInfo("banner_returned_to_application", String::class.java))
        signals.add(SignalInfo("banner_on_impression", String::class.java, String::class.java))
        // interstitial
        signals.add(SignalInfo("interstitial_loaded", String::class.java))
        signals.add(SignalInfo("interstitial_failed_to_load", String::class.java, Integer::class.java))
        signals.add(SignalInfo("interstitial_failed_to_show", String::class.java, Integer::class.java))
        signals.add(SignalInfo("interstitial_ad_shown", String::class.java))
        signals.add(SignalInfo("interstitial_ad_dismissed", String::class.java))
        signals.add(SignalInfo("interstitial_ad_clicked", String::class.java))
        signals.add(SignalInfo("interstitial_on_impression", String::class.java, String::class.java))
        //rewarded
        signals.add(SignalInfo("rewarded_loaded", String::class.java))
        signals.add(SignalInfo("rewarded_failed_to_load", String::class.java, Integer::class.java))
        signals.add(SignalInfo("rewarded_ad_shown", String::class.java))
        signals.add(SignalInfo("rewarded_ad_dismissed", String::class.java))
        signals.add(SignalInfo("rewarded_rewarded", String::class.java, Dictionary::class.java))
        signals.add(SignalInfo("rewarded_ad_clicked", String::class.java))
        signals.add(SignalInfo("rewarded_on_impression", String::class.java, String::class.java))
        return signals
    }

    override fun onMainCreate(activity: Activity): View? {
        layout = FrameLayout(activity)
        return layout
    }

    @UsedByGodot
    fun init() {
        MobileAds.initialize(godot.getActivity()!!) {
            emitSignal("ads_initialized")
        }
    }

    @UsedByGodot
    fun enableLogging(value: Boolean) {
        MobileAds.enableLogging(value)
    }

    @UsedByGodot
    fun setAdGroupPreloading(value: Boolean) {
        MobileInstreamAds.setAdGroupPreloading(value)
    }

    @UsedByGodot
    fun setUserConsent(value: Boolean) {
        MobileAds.setUserConsent(value)
    }

    @UsedByGodot
    fun setLocationConsent(value: Boolean) {
        MobileAds.setLocationConsent(value)
    }

    @UsedByGodot
    fun setAgeRestrictedUser(value: Boolean) {
        MobileAds.setAgeRestrictedUser(value)
    }

    private fun request(): AdRequest {
        return AdRequest.Builder().build()
    }

    @UsedByGodot
    fun loadBanner(id: String, params: Dictionary) {
        godot.getActivity()?.runOnUiThread {
            if (!banners.containsKey(id) || banners[id] == null) {
                createBanner(id, params)
            } else {
                banners[id]?.loadAd(request())
            }
        }
    }

    private fun createBanner(id: String, params: Dictionary) {
//        layout = godot.getActivity()!!.window.decorView.rootView as FrameLayout

        val activity = activity
        if (activity == null) {
            Log.w(tag, "activity is null")
            return
        }

        val banner = BannerAdView(activity)

        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        val position = params.getOrDefault(BANNER_POSITION, POSITION_BOTTOM) as Int
        val safearea = params.getOrDefault(BANNER_SAFE_AREA, true) as Boolean

        if (position == POSITION_TOP) {
            layoutParams?.gravity = Gravity.TOP
            if (safearea) banner.y = getSafeArea().top.toFloat()
        } else { // default
            layoutParams?.gravity = Gravity.BOTTOM
            if (safearea) banner.y = (-getSafeArea().bottom).toFloat()
        }

        var sizeType = params.getOrDefault(BANNER_SIZE_TYPE, BANNER_STICKY_SIZE)
        var width = params.getOrDefault(BANNER_WIDTH, 0) as Int
        var height = params.getOrDefault(BANNER_HEIGHT, 0) as Int

        when (sizeType) {
            BANNER_INLINE_SIZE ->
                banner.setAdSize(BannerAdSize.inlineSize(activity, width, height))

            BANNER_STICKY_SIZE ->
                banner.setAdSize(BannerAdSize.stickySize(activity, width))
        }

        banner.setBannerAdEventListener(object : BannerAdEventListener {
            override fun onAdLoaded() {
                Log.w(tag, "onBannerAdLoaded")
                emitSignal("banner_loaded", id)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                Log.w(tag, "onBannerAdFailedToLoad. Error: " + error.code)
                emitSignal("banner_failed_to_load", id, error.code)
            }

            override fun onAdClicked() {
                Log.w(tag, "onBannerAdClicked")
                emitSignal("banner_ad_clicked", id)
            }

            override fun onLeftApplication() {
                Log.w(tag, "onBannerLeftApplication")
                emitSignal("banner_left_application", id)
            }

            override fun onReturnedToApplication() {
                Log.w(tag, "onBannerReturnedToApplication")
                emitSignal("banner_returned_to_application", id)
            }

            override fun onImpression(impression: ImpressionData?) {
                Log.w(tag, "onBannerAdImpression");
                emitSignal("banner_on_impression", id, impression?.rawData.orEmpty());
            }
        })

        banner.setAdUnitId(id);
        banner.setBackgroundColor(Color.TRANSPARENT);

        banners[id] = banner

        layout.addView(banner, layoutParams);
        banner.loadAd(request());
    }

    @UsedByGodot
    fun removeBanner(id: String) {
        godot.getActivity()?.runOnUiThread {
            if (banners.containsKey(id) && banners[id] != null) {
                layout.removeView(banners[id]) // Remove the banner
                banners.remove(id)
                Log.d(tag, "removeBanner: banner ok")
            } else {
                Log.w(tag, "removeBanner: banner not found")
            }
        }
    }

    @UsedByGodot
    fun showBanner(id: String) {
        godot.getActivity()?.runOnUiThread {
            if (banners.containsKey(id) && banners[id] != null) {
                banners[id]?.visibility = View.VISIBLE
                Log.d(tag, "showBanner: banner ok")
            } else {
                Log.w(tag, "showBanner: banner not found")
            }
        }
    }

    @UsedByGodot
    fun hideBanner(id: String) {
        if (banners.containsKey(id) && banners[id] != null) {
            banners[id]?.visibility = View.GONE
            Log.d(tag, "hideBanner: banner ok")
        } else {
            Log.w(tag, "hideBanner: banner not found")
        }
    }

    @UsedByGodot
    fun loadInterstitial(id: String) {
        godot.getActivity()?.runOnUiThread {
            createInterstitial(id)
        }
    }

    private fun createInterstitial(id: String) {
        val activity = activity
        if (activity == null) {
            Log.w(tag, "activity is null")
            return
        }

        val loader = InterstitialAdLoader(activity)
        loader.setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(interstitial: InterstitialAd) {
                Log.w(tag, "onInterstitialAdLoaded")

                emitSignal("interstitial_loaded", id)

                interstitial.setAdEventListener(object : InterstitialAdEventListener {
                    override fun onAdShown() {
                        Log.w(tag, "onInterstitialAdShown")
                        emitSignal("interstitial_ad_shown", id)
                    }

                    override fun onAdFailedToShow(error: AdError) {
                        Log.w(tag, "onInterstitialAdFailedToShow: ${error.description}")
                        emitSignal("interstitial_failed_to_show", id, error.description)
                    }

                    override fun onAdDismissed() {
                        Log.w(tag, "onInterstitialAdDismissed")
                        emitSignal("interstitial_ad_dismissed", id)
                    }

                    override fun onAdClicked() {
                        Log.w(tag, "onInterstitialAdClicked")
                        emitSignal("interstitial_ad_clicked", id)
                    }

                    override fun onAdImpression(data: ImpressionData?) {
                        Log.w(tag, "onInterstitialAdImpression: ${data?.rawData.orEmpty()}")
                        emitSignal("interstitial_on_impression", id, data?.rawData.orEmpty())
                    }
                })

                interstitials[id] = interstitial
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                Log.w(tag, "onAdFailedToLoad. error: " + error.code)
                emitSignal("interstitial_failed_to_load", id, error.description)
            }
        })
        loader.loadAd(AdRequestConfiguration.Builder(id).build())
    }

    @UsedByGodot
    fun showInterstitial(id: String) {
        val activity = activity
        if (activity == null) {
            Log.w(tag, "activity is null")
            return
        }

        godot.getActivity()?.runOnUiThread {
            if (interstitials.containsKey(id) && interstitials[id] != null) {
                interstitials[id]?.show(activity)
                Log.d(tag, "showInterstitial: interstitial ok");
            } else {
                Log.w(tag, "showInterstitial: interstitial not found");
            }
        }
    }

    @UsedByGodot
    fun loadRewarded(id: String) {
        godot.getActivity()?.runOnUiThread {
            createRewarded(id)
        }
    }

    private fun createRewarded(id: String) {
        val loader = RewardedAdLoader(godot.getActivity()!!)

        loader.setAdLoadListener(object : RewardedAdLoadListener {
            override fun onAdLoaded(rewarded: RewardedAd) {
                Log.w(tag, "onAdLoaded")

                emitSignal("rewarded_loaded", id)

                rewarded.setAdEventListener(object : RewardedAdEventListener {
                    override fun onAdShown() {
                        Log.w(tag, "onAdShown")
                        emitSignal("rewarded_ad_shown", id)
                    }

                    override fun onAdFailedToShow(error: AdError) {
                        Log.w(tag, "onAdFailedToShow. error: ${error.description}")
                        emitSignal("rewarded_ad_shown", id)
                    }

                    override fun onAdDismissed() {
                        Log.w(tag, "onAdDismissed")
                        emitSignal("rewarded_ad_dismissed", id)
                    }

                    override fun onRewarded(reward: Reward) {
                        Log.w(tag, "YandexAds: onRewarded")
                        val data = Dictionary()
                        data.set("amount", reward.amount)
                        data.set("type", reward.type)
                        emitSignal("rewarded_rewarded", id, data)
                    }

                    override fun onAdClicked() {
                        Log.w(tag, "onAdClicked")
                        emitSignal("rewarded_ad_clicked", id)
                    }

                    override fun onAdImpression(impression: ImpressionData?) {
                        Log.w(tag, "onAdImpression")
                        emitSignal("rewarded_on_impression", id, impression?.rawData.orEmpty())
                    }

                })

                rewardeds[id] = rewarded
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                Log.w(tag, "onAdFailedToLoad. error: " + error.code)
                emitSignal("rewarded_failed_to_load", id, error.description)
            }

        })
        loader.loadAd(AdRequestConfiguration.Builder(id).build())
    }

    @UsedByGodot
    fun showRewarded(id: String) {
        godot.getActivity()?.runOnUiThread {
            if (rewardeds.containsKey(id) && rewardeds[id] != null) {
                rewardeds[id]?.show(godot.getActivity()!!)
            } else {
                Log.w(tag, "showRewarded");
            }
        }
    }

    private fun getSafeArea(): Rect {
        val safeInsetRect = Rect()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return safeInsetRect
        }
        val windowInsets: WindowInsets = godot.getActivity()!!.getWindow().getDecorView().getRootWindowInsets()
            ?: return safeInsetRect
        val displayCutout = windowInsets.displayCutout
        if (displayCutout != null) {
            safeInsetRect[displayCutout.safeInsetLeft, displayCutout.safeInsetTop, displayCutout.safeInsetRight] =
                displayCutout.safeInsetBottom
        }
        return safeInsetRect
    }

    companion object {
        const val POSITION_TOP = 1
        const val POSITION_BOTTOM = 0

        const val BANNER_STICKY_SIZE = "sticky"
        const val BANNER_INLINE_SIZE = "inline"

        const val BANNER_POSITION = "position"
        const val BANNER_SAFE_AREA = "safe_area"
        const val BANNER_WIDTH = "width"
        const val BANNER_HEIGHT = "height"
        const val BANNER_SIZE_TYPE = "size_type"
    }
}
