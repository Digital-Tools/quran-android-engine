plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.model.qurantext"
}

dependencies {
    implementation(project(":model:quran-kit"))
}
