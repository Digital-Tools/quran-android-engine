plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":data:audio-timing-persistence"))
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-audio"))
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}

android {
    namespace = "com.quranengine.domain.audiotimingservice"
}
