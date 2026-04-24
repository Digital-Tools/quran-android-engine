plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.model.quranaudio"
}

dependencies {
    implementation(project(":model:quran-kit"))
}
