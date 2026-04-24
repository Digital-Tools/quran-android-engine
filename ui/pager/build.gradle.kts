plugins { id("quranengine.compose.library") }

android { namespace = "com.quranengine.ui.pager" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}
