pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "quran-android-engine"

// Model modules
include(":model:quran-kit")
include(":model:quran-audio")
include(":model:quran-text")
include(":model:quran-annotations")
include(":model:quran-geometry")

// Core modules
include(":core:preferences")
include(":core:audio-player")
include(":core:localization")
include(":core:caching")
include(":core:system")
include(":core:utilities")

// Data modules
include(":data:sqlite-persistence")
include(":data:annotation-persistence")
include(":data:network")
include(":data:batch-downloader")
include(":data:verse-text-persistence")
include(":data:translation-persistence")
include(":data:audio-timing-persistence")
include(":data:word-frame-persistence")
include(":data:word-text-persistence")

// Domain modules
include(":domain:quran-text-kit")
include(":domain:quran-audio-kit")
include(":domain:reading-service")
include(":domain:translation-service")
include(":domain:reciter-service")
include(":domain:annotation-service")
include(":domain:image-service")
include(":domain:word-frame-service")
include(":domain:word-text-service")
include(":domain:settings-service")
include(":domain:audio-timing-service")

// UI modules
include(":ui:theme")
include(":ui:components")
include(":ui:quran")
include(":ui:audio-banner")
include(":ui:pager")

// Feature modules
include(":features:app-structure")
include(":features:home")
include(":features:quran-view")
include(":features:quran-pages")
include(":features:quran-image")
include(":features:quran-content")
include(":features:quran-translation")
include(":features:translation-verse")
include(":features:audio-banner")
include(":features:bookmarks")
include(":features:search")
include(":features:reciter-list")
include(":features:translations")
include(":features:settings")
include(":features:advanced-audio")
include(":features:word-pointer")

// App
include(":app")
