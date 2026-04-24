plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-audio"))
    implementation(project(":data:batch-downloader"))
    implementation(project(":data:audio-timing-persistence"))
    implementation(project(":domain:reciter-service"))
    implementation(project(":domain:audio-timing-service"))
    implementation(project(":domain:quran-text-kit"))
    implementation(project(":core:audio-player"))
    implementation(project(":core:localization"))
    implementation(project(":core:preferences"))
    implementation(project(":core:system"))
    implementation(project(":core:utilities"))
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.common)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}

android {
    namespace = "com.quranengine.domain.quranaudiokit"
}
