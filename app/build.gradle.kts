plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.quranengine.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.quranengine.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

hilt {
    enableAggregatingTask = false
}

dependencies {
    // Model
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-audio"))
    implementation(project(":model:quran-text"))
    implementation(project(":model:quran-annotations"))
    implementation(project(":model:quran-geometry"))

    // Core
    implementation(project(":core:preferences"))
    implementation(project(":core:audio-player"))
    implementation(project(":core:localization"))
    implementation(project(":core:caching"))
    implementation(project(":core:system"))
    implementation(project(":core:utilities"))

    // Data
    implementation(project(":data:sqlite-persistence"))
    implementation(project(":data:annotation-persistence"))
    implementation(project(":data:network"))
    implementation(project(":data:batch-downloader"))
    implementation(project(":data:audio-timing-persistence"))
    implementation(project(":data:translation-persistence"))
    implementation(project(":data:verse-text-persistence"))

    // Domain
    implementation(project(":domain:quran-text-kit"))
    implementation(project(":domain:quran-audio-kit"))
    implementation(project(":domain:audio-timing-service"))
    implementation(project(":domain:reading-service"))
    implementation(project(":domain:translation-service"))
    implementation(project(":domain:reciter-service"))
    implementation(project(":domain:annotation-service"))
    implementation(project(":domain:image-service"))
    implementation(project(":domain:settings-service"))

    // UI
    implementation(project(":ui:theme"))
    implementation(project(":ui:components"))
    implementation(project(":ui:quran"))
    implementation(project(":ui:audio-banner"))
    implementation(project(":ui:pager"))

    // Features
    implementation(project(":features:app-structure"))
    implementation(project(":features:home"))
    implementation(project(":features:quran-view"))
    implementation(project(":features:quran-pages"))
    implementation(project(":features:quran-image"))
    implementation(project(":features:quran-content"))
    implementation(project(":features:quran-translation"))
    implementation(project(":features:audio-banner"))
    implementation(project(":features:bookmarks"))
    implementation(project(":features:search"))
    implementation(project(":features:reciter-list"))
    implementation(project(":features:translations"))
    implementation(project(":features:settings"))
    implementation(project(":features:advanced-audio"))
    implementation(project(":features:word-pointer"))

    // AndroidX + Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.core.ktx)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.room.runtime)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.okhttp)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Logging
    implementation(libs.timber)

    // Debug
    debugImplementation(libs.compose.ui.tooling)

    // Testing
    testImplementation(libs.junit)
}
