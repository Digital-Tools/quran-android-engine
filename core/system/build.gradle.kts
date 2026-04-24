plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.core.system"
}

dependencies {
    implementation(project(":core:utilities"))
    implementation(libs.coroutines.core)
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
