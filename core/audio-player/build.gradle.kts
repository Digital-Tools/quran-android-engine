plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.core.audioplayer"
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
