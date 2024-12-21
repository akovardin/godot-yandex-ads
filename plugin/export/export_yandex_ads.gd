@tool
extends EditorPlugin

# A class member to hold the editor export plugin during its lifecycle.
var export_plugin : AndroidExportPlugin

func _enter_tree():
	# Initialization of the plugin goes here.
	export_plugin = AndroidExportPlugin.new()
	add_export_plugin(export_plugin)


func _exit_tree():
	# Clean-up of the plugin goes here.
	remove_export_plugin(export_plugin)
	export_plugin = null


class AndroidExportPlugin extends EditorExportPlugin:
	var _plugin_name = "GodotYandexAds"

	func _supports_platform(platform):
		if platform is EditorExportPlatformAndroid:
			return true
		return false

	func _get_android_libraries(platform, debug):
		if debug:
			return PackedStringArray([_plugin_name + "/bin/debug/" + _plugin_name + "-debug.aar"])
		else:
			return PackedStringArray([_plugin_name + "/bin/release/" + _plugin_name + "-release.aar"])

	func _get_android_dependencies(platform, debug):
		return PackedStringArray([
			"com.yandex.android:mobileads-mediation:7.8.0.0"
		])


	func _get_android_dependencies_maven_repos(platform, debug):
		return PackedStringArray([
			"https://android-sdk.is.com/",
			"https://artifact.bytedance.com/repository/pangle",
			"https://sdk.tapjoy.com/",
			"https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea",
			"https://cboost.jfrog.io/artifactory/chartboost-ads/",
			"https://dl.appnext.com/"
		])

	func _get_android_manifest_application_element_contents(platform, debug) -> String:
		return '<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="ca-app-pub-3940256099942544~3347511713"/>'


	func _get_name():
		return _plugin_name
