plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.model.quranannotations"
}

dependencies {
    implementation(project(":model:quran-kit"))
}
