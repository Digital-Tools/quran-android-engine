plugins { id("quranengine.compose.library") }

android { namespace = "com.quranengine.features.translationverse" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(project(":ui:components"))
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-text"))
    implementation(project(":domain:quran-text-kit"))
    implementation(project(":features:quran-translation"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}
