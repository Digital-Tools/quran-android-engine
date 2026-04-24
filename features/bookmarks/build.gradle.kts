plugins {
    id("quranengine.compose.library")
    alias(libs.plugins.ksp)
}

android { namespace = "com.quranengine.features.bookmarks" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(project(":ui:components"))
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-annotations"))
    implementation(project(":domain:annotation-service"))
    implementation(project(":domain:quran-text-kit"))
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
