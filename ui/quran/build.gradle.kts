plugins { id("quranengine.compose.library") }

android { namespace = "com.quranengine.ui.quran" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-geometry"))
    implementation(project(":model:quran-annotations"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
    debugImplementation(libs.compose.ui.tooling)
}
