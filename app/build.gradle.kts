plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    // id("com.google.firebase.firebase-perf")
}

android {
    compileSdk = Versions.Android.compileSdk

    defaultConfig {
        applicationId = "cz.frantisekmasa.dnd"
        minSdk = Versions.Android.minSdk
        targetSdk = Versions.Android.targetSdk
        versionCode = System.getenv("SUPPLY_VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("SUPPLY_VERSION_NAME") ?: "dev"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = File(System.getProperty("user.dir") + "/app/.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "uploadKey"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "[Debug] WFRP Master")
            resValue("string", "character_ad_unit_id", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "game_master_ad_unit_id", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "combat_ad_unit_id", "ca-app-pub-3940256099942544/6300978111")

            addManifestPlaceholders(
                mapOf(
                    "analytics_activated" to "false",
                    "usesCleartextTraffic" to "true",
                )
            )
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            signingConfig = signingConfigs.getByName("release")

            resValue("string", "app_name", "WFRP Master")
            resValue("string", "character_ad_unit_id", "ca-app-pub-8647604386686373/9919978313")
            resValue("string", "game_master_ad_unit_id", "ca-app-pub-8647604386686373/7714574658")
            resValue("string", "combat_ad_unit_id", "ca-app-pub-8647604386686373/3858132571")

            addManifestPlaceholders(
                mapOf(
                    "analytics_activated" to "true",
                    "usesCleartextTraffic" to "false",
                )
            )
        }
    }

    compileOptions {
        // Allow use of Java 8 APIs on older Android versions
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs +
            "-Xskip-prerelease-check" +
            "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi" +
            "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi" +
            "-P" +
            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
    }
}

dependencies {
    implementation(project(":common"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // Allow use of Java 8 APIs on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.2.2")

    // Testing utilities
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("org.mockito:mockito-core:2.7.22")

    // Basic Android stuff
    api("androidx.core:core-ktx:1.9.0")
    api("androidx.fragment:fragment-ktx:1.5.6")

    // Coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
}
