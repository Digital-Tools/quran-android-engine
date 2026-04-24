plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.core.caching"
}

dependencies {
    implementation(project(":core:utilities"))
    implementation(libs.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
}
