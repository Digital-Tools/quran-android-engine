plugins {
    id("quranengine.compose.library")
    alias(libs.plugins.ksp)
}

android { namespace = "com.quranengine.features.translations" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(project(":ui:components"))
    implementation(project(":model:quran-text"))
    implementation(project(":domain:translation-service"))
    implementation(project(":data:batch-downloader"))
    implementation(project(":core:localization"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
