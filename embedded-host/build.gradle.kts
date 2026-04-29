plugins {
    id("quranengine.compose.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

group = "com.quranengine"
version = "0.1.0"

android {
    namespace = "com.quranengine.embeddedhost"

    sourceSets {
        getByName("main") {
            assets.srcDir("../app/src/main/assets")
        }
    }
}

hilt {
    enableAggregatingTask = false
}

dependencies {
    api(project(":model:quran-kit"))
    api(project(":model:quran-audio"))
    api(project(":model:quran-text"))
    api(project(":model:quran-annotations"))
    api(project(":model:quran-geometry"))

    api(project(":core:preferences"))
    api(project(":core:audio-player"))
    api(project(":core:localization"))
    api(project(":core:caching"))
    api(project(":core:system"))
    api(project(":core:utilities"))

    api(project(":data:sqlite-persistence"))
    api(project(":data:annotation-persistence"))
    api(project(":data:network"))
    api(project(":data:batch-downloader"))
    api(project(":data:audio-timing-persistence"))
    api(project(":data:translation-persistence"))
    api(project(":data:verse-text-persistence"))

    api(project(":domain:quran-text-kit"))
    api(project(":domain:quran-audio-kit"))
    api(project(":domain:audio-timing-service"))
    api(project(":domain:reading-service"))
    api(project(":domain:translation-service"))
    api(project(":domain:reciter-service"))
    api(project(":domain:annotation-service"))
    api(project(":domain:image-service"))
    api(project(":domain:settings-service"))

    api(project(":ui:theme"))
    api(project(":ui:components"))
    api(project(":ui:quran"))
    api(project(":ui:audio-banner"))
    api(project(":ui:pager"))

    api(project(":features:app-structure"))
    api(project(":features:home"))
    api(project(":features:quran-view"))
    api(project(":features:quran-pages"))
    api(project(":features:quran-image"))
    api(project(":features:quran-content"))
    api(project(":features:quran-translation"))
    api(project(":features:audio-banner"))
    api(project(":features:bookmarks"))
    api(project(":features:search"))
    api(project(":features:reciter-list"))
    api(project(":features:translations"))
    api(project(":features:settings"))
    api(project(":features:advanced-audio"))
    api(project(":features:word-pointer"))

    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.material3)
    api(libs.compose.ui.tooling.preview)
    api(libs.activity.compose)
    api(libs.navigation.compose)
    api(libs.lifecycle.runtime.compose)
    api(libs.lifecycle.viewmodel.compose)
    api(libs.core.ktx)
    api(libs.media3.exoplayer)
    api(libs.media3.session)
    api(libs.room.runtime)
    api(libs.ktor.client.core)
    api(libs.ktor.client.okhttp)
    api(libs.okhttp)

    api(libs.hilt.android)
    api(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    api(libs.timber)

    debugImplementation(libs.compose.ui.tooling)
    testImplementation(libs.junit)
}
