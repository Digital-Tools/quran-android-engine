plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.core.localization"
}

dependencies {
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
