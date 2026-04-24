plugins { id("quranengine.compose.library") }

android { namespace = "com.quranengine.ui.components" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(project(":model:quran-text"))
    implementation(project(":core:localization"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
    debugImplementation(libs.compose.ui.tooling)
}
