plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":core:localization"))

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.coroutines.core)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

android {
    namespace = "com.quranengine.data.network"
}
