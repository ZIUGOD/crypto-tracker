import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.example.crypto_tracker"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Production package. It must be registered in the Android OAuth client.
        applicationId = "com.cryptotracker.app"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // A Web OAuth client ID identifies the backend that receives the Google ID
        // token. Keep it configurable per developer/CI environment: an OAuth client
        // from another Google Cloud project will fail after the account is selected.
        val defaultGoogleWebClientId = "991627423046-1h46ij6ki5f4722t3bqbar13euvbq80m.apps.googleusercontent.com"
        val googleWebClientId = providers.gradleProperty("GOOGLE_WEB_CLIENT_ID")
            .orElse(defaultGoogleWebClientId)
            .get()
        resValue("string", "google_web_client_id", googleWebClientId)
    }

    // Upload key for the Play Store. Credentials live in keystore.properties (project root, git-ignored);
    // without that file the release build is simply left unsigned. See PLAY_RELEASE.md.
    val keystoreProperties = Properties().apply {
        val file = rootProject.file("keystore.properties")
        if (file.isFile) file.inputStream().use { load(it) }
    }
    signingConfigs {
        if (keystoreProperties.containsKey("storeFile")) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        // AdMob: debug always uses Google's sample IDs so development never produces real impressions
        // or clicks (policy violation); only release uses the production IDs.
        debug {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        }
        release {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-9399548772812079~1855065116"
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"ca-app-pub-9399548772812079/3355763327\"")
            signingConfigs.findByName("release")?.let { signingConfig = it }
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // The user picks the language inside the app (Settings), so every language must ship in the base
    // APK. With Play's per-language splits, a language the device did not request would be missing.
    bundle {
        language {
            enableSplit = false
        }
    }
    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.haze)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    testImplementation(libs.junit)
    testImplementation(libs.org.json)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
