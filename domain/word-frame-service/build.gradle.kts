plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":data:word-frame-persistence"))
    implementation(project(":model:quran-geometry"))
    implementation(project(":model:quran-kit"))
}

android {
    namespace = "com.quranengine.domain.wordframeservice"
}
