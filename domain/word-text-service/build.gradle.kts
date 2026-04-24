plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":model:quran-text"))
    implementation(project(":model:quran-kit"))
    implementation(project(":data:word-text-persistence"))
    implementation(project(":core:preferences"))
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}

android {
    namespace = "com.quranengine.domain.wordtextservice"
}
