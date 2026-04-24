plugins { id("quranengine.compose.library") }

android { namespace = "com.quranengine.features.wordpointer" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(project(":ui:quran"))
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-geometry"))
    implementation(project(":domain:word-frame-service"))
    implementation(project(":domain:word-text-service"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.animation)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}
