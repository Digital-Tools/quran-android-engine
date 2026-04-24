plugins { id("quranengine.compose.library") }

android { namespace = "com.quranengine.features.quranpages" }

dependencies {
    implementation(project(":ui:theme"))
    implementation(project(":ui:pager"))
    implementation(project(":ui:quran"))
    implementation(project(":features:quran-image"))
    implementation(project(":features:quran-translation"))
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-text"))
    implementation(project(":domain:reading-service"))
    implementation(project(":domain:quran-text-kit"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}
