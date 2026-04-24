plugins {
    id("quranengine.android.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":model:quran-text"))
    implementation(project(":data:translation-persistence"))
    implementation(project(":data:verse-text-persistence"))
    implementation(project(":data:batch-downloader"))
    implementation(project(":data:network"))
    implementation(project(":core:localization"))
    implementation(project(":core:preferences"))
    implementation(project(":core:system"))
    implementation(project(":core:utilities"))
    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}

android {
    namespace = "com.quranengine.domain.translationservice"
}
