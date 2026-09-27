plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.core.audioplayer"

    // SoundTouch time-stretcher for pitch-preserving playback speed (src/main/cpp).
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(project(":core:utilities"))
    implementation(project(":core:system"))
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.common)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
}
