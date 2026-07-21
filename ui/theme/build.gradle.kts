plugins { id("quranengine.compose.library") }

android { namespace = "com.quranengine.ui.theme" }

dependencies {
    implementation(project(":core:preferences"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.coroutines.core)
    implementation(libs.core.ktx)
    debugImplementation(libs.compose.ui.tooling)
}
