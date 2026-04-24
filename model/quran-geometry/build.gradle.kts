plugins {
    id("quranengine.android.library")
}

android {
    namespace = "com.quranengine.model.qurangeometry"
}

dependencies {
    implementation(project(":model:quran-kit"))
}
