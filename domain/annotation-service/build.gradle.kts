plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":model:quran-annotations"))
    implementation(project(":model:quran-kit"))
    implementation(project(":data:annotation-persistence"))
    implementation(project(":domain:quran-text-kit"))
    implementation(project(":core:localization"))
    implementation(project(":core:preferences"))
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}

android {
    namespace = "com.quranengine.domain.annotationservice"
}
