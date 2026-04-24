plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.model.qurankit"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
