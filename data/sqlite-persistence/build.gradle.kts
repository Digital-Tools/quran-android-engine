plugins {
    id("quranengine.android.library")
}

dependencies {
    implementation(project(":core:utilities"))
    implementation(project(":core:localization"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

android {
    namespace = "com.quranengine.data.sqlitepersistence"
}
