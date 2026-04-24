plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":core:utilities"))
    implementation(project(":data:sqlite-persistence"))
    implementation(project(":model:quran-kit"))
    implementation(project(":model:quran-audio"))

    implementation(libs.coroutines.core)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

android {
    namespace = "com.quranengine.data.audiotimingpersistence"
}
