plugins {
    id("quranengine.android.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core:utilities"))
    implementation(project(":core:system"))
    implementation(project(":data:sqlite-persistence"))
    implementation(project(":data:network"))

    implementation(libs.okhttp)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

android {
    namespace = "com.quranengine.data.batchdownloader"
}
