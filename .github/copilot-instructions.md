# Copilot instructions for QuranEngine Android

## Required context workflow

- If an `llm-wiki` or internal project wiki is available to you, review the relevant concept pages (e.g., `quran-android-engine.md`, `build-commands.md`) before making repository changes.
- Ensure you understand the architecture outlined in this document and follow the provided module structures and dependencies.
- If code changes affect core concepts, ensure the relevant wiki pages and logs are updated to keep the knowledge base current.

## Build, test, and lint

- Use the Gradle wrapper from the repository root. The project uses Gradle KTS, AGP 8.11.1, Kotlin 2.0.21, compile SDK 35, min SDK 24, and JVM 17 bytecode. Library modules request a JDK 21 Kotlin toolchain in `buildSrc`.
- Assemble the debug app: `./gradlew :app:assembleDebug`
- Install on the active emulator/device: `./gradlew :app:installDebug`
- Launch after install: `adb shell am start -n com.quranengine.app/com.quranengine.embeddedhost.QuranHostActivity`
- Launch deep links:
  - `adb shell am start -a android.intent.action.VIEW -d 'quranengine://page/2' com.quranengine.app`
  - `adb shell am start -a android.intent.action.VIEW -d 'quranengine://search?q=mercy' com.quranengine.app`
- Run all unit tests: `./gradlew test`
- Run the CI target: `./gradlew --no-daemon :app:assembleDebug :app:testDebugUnitTest`
- Run a module's unit tests: `./gradlew :core:localization:testDebugUnitTest`
- Run one test class or method:
  - `./gradlew :features:app-structure:testDebugUnitTest --tests 'com.quranengine.features.appstructure.DeepLinkHandlerTest'`
  - `./gradlew :features:app-structure:testDebugUnitTest --tests 'com.quranengine.features.appstructure.DeepLinkHandlerTest.maps page deep link to quran route'`
- Run Android lint: `./gradlew lint` or scope it to the app with `./gradlew :app:lintDebug`.
- No ktlint, detekt, Spotless, or custom formatter configuration is currently present.

## Architecture

- This is the active standalone Android source of truth for QuranEngine, a Kotlin/Jetpack Compose port of the iOS engine. It is also embedded by `mizan-app` through a composite Gradle build that consumes `com.quranengine:embedded-host:0.1.0`.
- Keep dependencies flowing downward: `features` -> `ui`/`domain` -> `data`/`core` -> `model`.
- `model/*` contains pure Quran/audio/text/annotation/geometry types. Quran navigation objects such as `Quran`, `Sura`, `Page`, and `AyahNumber` validate construction through nullable factory-style `invoke` functions and internal constructors.
- `core/*` contains platform abstractions and shared utilities: preferences, localization, system/file wrappers, caching, and Media3 audio-player adapters.
- `data/*` contains persistence and network adapters. Mutable app/download state uses Room; Quran text, translation, timing, word text, and word frame data often use read-only SQLite adapters around bundled or downloaded database files.
- `domain/*` contains services and preference wrappers that compose model, data, and core dependencies. Many settings expose both a current property and a Flow backed by `Preferences.notifications`.
- `ui/*` contains reusable Compose design-system pieces. Prefer existing `QuranTheme`, `ui:components`, `ui:quran`, `ui:pager`, and `ui:audio-banner` primitives before adding feature-local UI building blocks.
- `features/*` contains Compose screens, routes, and ViewModels. Feature ViewModels usually own `MutableStateFlow` state and are provided by Hilt from the host/navigation layer.
- `embedded-host` exports `QuranHostActivity`, `QuranHostContract`, `QuranHostScreen`, `MainViewModel`, and the Hilt modules (`CoreModule`, `DataModule`, `DomainModule`). `app` is a thin standalone shell that depends on `embedded-host`.

## Project-specific conventions

- Reuse `buildSrc` convention plugins: apply `quranengine.android.library` for Android library modules and `quranengine.compose.library` for Compose library modules. Module namespaces follow the source package, for example `com.quranengine.features.quranview`.
- Declare dependencies explicitly per module using project dependencies and `gradle/libs.versions.toml`; root `build.gradle.kts` only declares shared plugin aliases not already on the `buildSrc` classpath.
- Keep Hilt bindings centralized in `embedded-host/src/main/kotlin/com/quranengine/app/di`. Add dependencies consumed by feature ViewModels or services there unless the dependency is truly feature-private.
- `embedded-host` reuses the standalone app's bundled Quran assets via `assets.srcDir("../app/src/main/assets")`. Reading assets live under `app/src/main/assets/readings/...`; `ReadingAssetsInstaller` copies bundled readings into app files with an `.installed-from-assets` marker before adapters read them.
- Deep links use the `quranengine://` scheme and are parsed by `features/app-structure/DeepLinkHandler.kt`; routes are represented by `AppRoute` and registered in `AppStructureScreen`.
- Compose routes should collect ViewModel state at the route layer and pass values/callbacks into screen composables, matching `QuranViewRoute`.
- Preferences should use `PreferenceKey`, `Preference`, and `TransformedPreference` wrappers instead of direct `SharedPreferences` access. If UI must react to changes, expose a Flow filtered from the preference notification key.
- Localization goes through `Localizer` (`l`, `lAndroid`, `lFormat`) and `MapLocalizer`; English fallback and key fallback are intentional behavior covered by `core/localization` tests.
- The reader/audio surface is already beyond a placeholder: Home, sura, quarter, bookmark-list, search, page bookmarks, pagination, inline translation mode, theme/appearance persistence, embedded `hafs_1405` assets, dark-mode Quran text legibility, and Juz-length default playback are documented in the wiki.
- Tests currently use JUnit 4 plus Truth and live in `src/test/kotlin`; there are no `src/androidTest` tests at the moment.
- `.github/workflows/android-ci.yml` is manual-only (`workflow_dispatch`). Push/PR triggers are intentionally disabled until standalone CI is restored for the composite-build/embedded-host setup.
