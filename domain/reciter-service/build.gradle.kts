plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-audio"))
    implementation(project(":core:localization"))
    implementation(project(":core:system"))
    implementation(project(":core:utilities"))
    implementation(project(":core:preferences"))
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}

android {
    namespace = "com.quranengine.domain.reciterservice"
}
