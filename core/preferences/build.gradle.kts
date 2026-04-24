plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.core.preferences"
}

dependencies {
    implementation(project(":core:utilities"))
    implementation(libs.coroutines.core)
    implementation(libs.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
