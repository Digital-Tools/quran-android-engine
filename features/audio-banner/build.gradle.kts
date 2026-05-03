plugins {
    id("quranengine.compose.library")
    alias(libs.plugins.ksp)
}

android { namespace = "com.quranengine.features.audiobanner" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(project(":ui:components"))
    implementation(project(":ui:audio-banner"))
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-audio"))
    implementation(project(":domain:quran-audio-kit"))
    implementation(project(":domain:reciter-service"))
    implementation(project(":core:audio-player"))
    implementation(project(":core:localization"))
    implementation(project(":data:batch-downloader"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.animation)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.timber)
}
