plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":domain:word-frame-service"))
    implementation(project(":data:word-frame-persistence"))
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-geometry"))
    implementation(project(":core:utilities"))
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}

android {
    namespace = "com.quranengine.domain.imageservice"
}
