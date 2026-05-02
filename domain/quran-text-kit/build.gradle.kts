plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-text"))
    implementation(project(":domain:translation-service"))
    implementation(project(":domain:word-frame-service"))
    implementation(project(":data:verse-text-persistence"))
    implementation(project(":core:preferences"))
    implementation(project(":core:utilities"))
    implementation(project(":core:localization"))
    implementation(libs.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}

android {
    namespace = "com.quranengine.domain.qurantextkit"
}
