plugins {
    id("quranengine.compose.library")
    alias(libs.plugins.ksp)
}

android { namespace = "com.quranengine.features.settings" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(project(":ui:components"))
    implementation(project(":domain:settings-service"))
    implementation(project(":domain:reading-service"))
    implementation(project(":domain:quran-text-kit"))
    implementation(project(":domain:quran-audio-kit"))
    implementation(project(":model:quran-audio"))
    implementation(project(":model:quran-text"))
    implementation(project(":model:quran-kit"))
    implementation(project(":core:preferences"))
    implementation(project(":core:localization"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
