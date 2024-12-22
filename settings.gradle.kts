pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://android-sdk.is.com/")
        maven("https://artifact.bytedance.com/repository/pangle")
        maven("https://sdk.tapjoy.com/")
        maven("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven("https://cboost.jfrog.io/artifactory/chartboost-ads/")
        maven("https://dl.appnext.com/")
    }
}

rootProject.name = "GodotYandexAds"
include(":plugin")
