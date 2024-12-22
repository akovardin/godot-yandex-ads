import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

// TODO: Update value to your plugin's name.
val pluginName = "GodotYandexAds"

// TODO: Update value to match your plugin's package name.
val pluginPackageName = "ru.kovardin.godotyandexads"

android {
    namespace = pluginPackageName
    compileSdk = 33

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24

        manifestPlaceholders["godotPluginName"] = pluginName
        manifestPlaceholders["godotPluginPackageName"] = pluginPackageName
        buildConfigField("String", "GODOT_PLUGIN_NAME", "\"${pluginName}\"")
        setProperty("archivesBaseName", pluginName)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("org.godotengine:godot:4.3.0.stable")

    implementation("com.yandex.android:mobileads-mediation:7.8.0.0")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
}

// BUILD TASKS DEFINITION
val copyDebugAARToDemoAddons by tasks.registering(Copy::class) {
    description = "Copies the generated debug AAR binary to the plugin's addons directory"
    from("build/outputs/aar")
    include("$pluginName-debug.aar")
    into("demo/addons/$pluginName/bin/debug")
}

val copyReleaseAARToDemoAddons by tasks.registering(Copy::class) {
    description = "Copies the generated release AAR binary to the plugin's addons directory"
    from("build/outputs/aar")
    include("$pluginName-release.aar")
    into("demo/addons/$pluginName/bin/release")
}

val cleanDemoAddons by tasks.registering(Delete::class) {
    delete("demo/addons/$pluginName")
}

val copyDebugAARToReleaseAddons by tasks.registering(Copy::class) {
    description = "Copies the generated debug AAR binary to the plugin's release addons directory"
    from("build/outputs/aar")
    include("$pluginName-debug.aar")
    into("release/addons/$pluginName/bin/debug")
}

val copyReleaseAARToReleaseAddons by tasks.registering(Copy::class) {
    description = "Copies the generated release AAR binary to the plugin's release addons directory"
    from("build/outputs/aar")
    include("$pluginName-release.aar")
    into("release/addons/$pluginName/bin/release")
}

val cleanReleaseAddons by tasks.registering(Delete::class) {
    delete("release/addons/$pluginName")
}

val copyAddonsToDemo by tasks.registering(Copy::class) {
    description = "Copies the export scripts templates to the plugin's addons directory"

    dependsOn(cleanDemoAddons)
    finalizedBy(copyDebugAARToDemoAddons)
    finalizedBy(copyReleaseAARToDemoAddons)

    from("export")
    into("demo/addons/$pluginName")
}

val copyAddonsToRelease by tasks.registering(Copy::class) {
    description = "Copies the export scripts templates to the plugin's addons directory"

    dependsOn(cleanReleaseAddons)
    finalizedBy(copyDebugAARToReleaseAddons)
    finalizedBy(copyReleaseAARToReleaseAddons)

    from("export")
    into("release/addons/$pluginName")
}

tasks.named("assemble").configure {
    finalizedBy(copyAddonsToDemo)
    finalizedBy(copyAddonsToRelease)
}

tasks.named<Delete>("clean").apply {
    dependsOn(cleanDemoAddons)
    dependsOn(cleanReleaseAddons)
}
