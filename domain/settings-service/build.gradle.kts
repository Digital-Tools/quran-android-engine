plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":core:preferences"))
    implementation(project(":core:utilities"))
    implementation(libs.coroutines.core)
}

android {
    namespace = "com.quranengine.domain.settingsservice"
}
