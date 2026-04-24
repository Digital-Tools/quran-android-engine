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
    implementation(project(":data:sqlite-persistence"))
    implementation(project(":model:quran-text"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.coroutines.core)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

android {
    namespace = "com.quranengine.data.translationpersistence"
}
