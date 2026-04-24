plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":model:quran-kit"))
    implementation(project(":core:preferences"))
    implementation(project(":core:utilities"))
    implementation(project(":core:system"))
    implementation(project(":data:batch-downloader"))
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}

android {
    namespace = "com.quranengine.domain.readingservice"
}
