
<p align="center">
    <strong>QuranEngine for Android</strong>
</p>

QuranEngine for Android
========================

QuranEngine for Android is the native Android port of the [QuranEngine iOS library](https://github.com/quran/quran-ios), built with Kotlin and Jetpack Compose. It provides a standalone Android-first foundation for building Quran-related applications on Android, with a modern architecture and idiomatic Android patterns.

> This library is ported from the iOS codebase and is open source under Apache-2.0.

## 🏗 Architecture

The library is organized as a **54-module** Gradle multi-module project across 7 layers:

```
quran-android-engine/
├── model/          5 modules — Pure Kotlin data classes
├── core/           6 modules — Platform abstractions
├── data/           9 modules — Persistence + networking
├── domain/        11 modules — Business logic
├── ui/             5 modules — Design system (Compose)
├── features/      17 modules — Screens (Compose)
└── app/            1 module  — Main app shell (Hilt DI)
```

Dependencies flow strictly downward: **Features → UI / Domain → Data / Core → Model**.

### Model (5 modules)
| Module | Description |
|--------|-------------|
| `quran-kit` | Sura, Ayah, Page, Juz, Hizb, Quarter, Word |
| `quran-audio` | Reciter, Timing, AudioEnd |
| `quran-text` | Translation, SearchResults, FontSize |
| `quran-annotations` | Note, PageBookmark, LastPage, Highlights |
| `quran-geometry` | WordFrame, WordFrameScale, ImagePage |

### Core (6 modules)
| Module | Description |
|--------|-------------|
| `preferences` | DataStore wrapper for app preferences |
| `system` | FileSystem, Keystore, Clock |
| `localization` | 16 languages via `strings.xml` |
| `caching` | LruCache + disk cache |
| `audio-player` | Media3 ExoPlayer wrapper |
| `utilities` | Kotlin extensions, coroutine helpers |

### Data (9 modules)
| Module | Description |
|--------|-------------|
| `sqlite-persistence` | Room DAOs for the Quran SQLite DB |
| `annotation-persistence` | Room DAOs — bookmarks, notes, last page |
| `translation-persistence` | Translation DB queries |
| `verse-text-persistence` | Arabic verse text queries |
| `word-text-persistence` | Word-by-word text |
| `word-frame-persistence` | Word position data |
| `audio-timing-persistence` | Audio timing data |
| `batch-downloader` | Background download service (WorkManager) |
| `network` | OkHttp/Ktor networking + reading/reciter/image data |

### Domain (11 modules)
| Module | Description |
|--------|-------------|
| `annotation-service` | Bookmarks, notes, highlights |
| `translation-service` | Translation download & management |
| `reciter-service` | Reciter list & configuration |
| `reading-service` | Reading modes & resources |
| `settings-service` | App settings |
| `image-service` | Page image loading |
| `word-text-service` | Word text lookup |
| `word-frame-service` | Word positioning |
| `audio-timing-service` | Audio timing resolution |
| `quran-text-kit` | Text loading, search, sharing |
| `quran-audio-kit` | Audio player, downloads, preferences |

### UI (5 modules)
| Module | Description |
|--------|-------------|
| `theme` | Material 3 theming, colors, typography |
| `components` | NoorListItem, MultipartText, shared composables |
| `quran` | QuranImage, Decorations, Separators |
| `pager` | HorizontalPager wrapper |
| `audio-banner` | Audio banner composable |

### Features (17 modules)
| Module | Description |
|--------|-------------|
| `app-structure` | Tabs & navigation shell |
| `home` | Sura list, last page resume |
| `quran-view` | Main reading screen |
| `quran-pages` | Page-by-page pager |
| `quran-image` | Image rendering |
| `quran-content` | Content loading orchestration |
| `quran-translation` | Translation overlay |
| `translation-verse` | Per-verse translation display |
| `audio-banner` | Audio playback controls |
| `bookmarks` | Bookmark list & management |
| `notes` | Note creation & management |
| `search` | Full-text search |
| `reciter-list` | Reciter selection |
| `translations` | Translation manager |
| `settings` | App settings screen |
| `advanced-audio` | Speed, repeat, range controls |
| `word-pointer` | Word-by-word highlighting |

### App (1 module)
Main application shell wiring all modules together with **Hilt** dependency injection.

## 🛠 Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.12.01) + Material 3 |
| Navigation | Compose Navigation with type-safe routes |
| DI | Hilt 2.53.1 |
| Database | Room 2.6.1 |
| Audio | Media3 1.5.1 (ExoPlayer) |
| Networking | Ktor 3.0.3 / OkHttp 4.12.0 |
| Async | Kotlin Coroutines 1.9.0 + Flow |
| Preferences | DataStore 1.1.1 |
| Image Loading | Coil 2.7.0 |
| Logging | Timber 5.0.1 |
| Build | Gradle KTS, multi-module, version catalog |

## 🎨 Design System

Five built-in themes with light and dark mode support, built on Material 3:

- **Calm** — Soft, muted tones
- **Focus** — High contrast for readability
- **Original** — Classic Quran.com appearance
- **Paper** — Warm, paper-like aesthetic
- **Quiet** — Minimal, subdued palette

## 🔧 Building

**Requirements:** Android Studio Hedgehog+ · JDK 21 · Min SDK 24 · Target SDK 35 · Compile SDK 35

```bash
# Assemble debug build
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Build a specific module
./gradlew :model:quran-kit:assembleDebug
```

The project can be built and tested directly from the `quran-android-engine/` root without requiring Swift or Xcode tooling.

## 🔗 Deep links

The app exposes a `quranengine://` scheme for launching common destinations directly into the standalone reader app:

```text
quranengine://page/2
quranengine://sura/2
quranengine://search?q=mercy
quranengine://bookmarks
quranengine://settings
quranengine://translations
quranengine://reciters
```

Page and sura links open the native reader, while search/settings/bookmarks-style links route into the existing tab or management screens.

## 📦 Module Convention Plugins

Convention plugins in `buildSrc/` standardize module configuration:

| Plugin | Purpose |
|--------|---------|
| `quranengine.android.library` | Base Android library setup (SDK versions, Kotlin config) |
| `quranengine.compose.library` | Adds Compose compiler + Material 3 dependencies |

Apply in any module's `build.gradle.kts`:
```kotlin
plugins {
    id("quranengine.android.library")
    id("quranengine.compose.library") // if the module uses Compose
}
```

## 📋 Status

This is an active, production-ready Android port of QuranEngine. All 54 core modules are implemented. The native reader flow currently covers:
- Navigation, pagination, and search
- Bookmarking and note-taking
- Inline translation and word-by-word highlighting
- Theme and appearance persistence
- Advanced audio (playback speed, word-highlight sync, reciter management)
- Deep-link routing

The engine is designed to be easily embedded into larger Android applications via AAR or Gradle composite builds, or run entirely standalone.

## 🤝 Contributions

We warmly welcome contributions to the Android engine!

1. Please start a conversation in the **Discussions** or **Issues** section before undertaking larger changes.
2. Fork the repository and create a branch using our convention (`feature/`, `fix/`, `chore/`, `docs/`).
3. Ensure you can pass `./gradlew test` locally.
4. Open a Pull Request. **CI status checks must pass** before your PR can be merged into `main`.

## 📄 License and attribution

- Repository-level distribution uses Apache-2.0. See [LICENSE](./LICENSE).
- See [NOTICE](./NOTICE) for project provenance and third-party attribution.
- Madani images are sourced from the [quran images project](https://github.com/quran/quran.com-images).
- Translation, tafsir, and Arabic data are attributed to [Tanzil](http://tanzil.net) and [King Saud University](https://quran.ksu.edu.sa).
- Third-party Gradle dependencies remain subject to their own licenses.
