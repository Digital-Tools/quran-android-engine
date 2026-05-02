package com.quranengine.app.di

import android.content.Context
import androidx.media3.session.MediaSession
import com.quranengine.core.audioplayer.NowPlayingUpdater
import com.quranengine.core.audioplayer.QueuePlayer
import com.quranengine.core.localization.Localizer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.SystemBundle
import com.quranengine.core.system.Zipper
import com.quranengine.data.batchdownloader.DownloadManager
import com.quranengine.data.annotation.persistence.LastPagePersistence
import com.quranengine.data.annotation.persistence.PageBookmarkPersistence
import com.quranengine.data.audiotiming.SqliteAyahTimingPersistence
import com.quranengine.data.network.NetworkManager
import com.quranengine.data.translation.SqliteActiveTranslationsPersistence
import com.quranengine.data.sqlite.ReadOnlyDatabase
import com.quranengine.data.versetext.SqliteDatabaseVersionPersistence
import com.quranengine.data.versetext.SqliteTranslationVerseTextPersistence
import com.quranengine.data.versetext.SqliteVerseTextPersistence
import com.quranengine.data.versetext.VerseTextPersistence
import com.quranengine.domain.annotationservice.LastPageService
import com.quranengine.domain.annotationservice.LastPageUpdater
import com.quranengine.domain.annotationservice.NoteService
import com.quranengine.domain.annotationservice.PageBookmarkService
import com.quranengine.data.annotation.persistence.NotePersistence
import com.quranengine.domain.quranaudiokit.AudioPreferences
import com.quranengine.domain.quranaudiokit.GaplessAudioRequestBuilder
import com.quranengine.domain.quranaudiokit.GappedAudioRequestBuilder
import com.quranengine.domain.quranaudiokit.PreferencesLastAyahFinder
import com.quranengine.domain.quranaudiokit.QuranAudioDownloader
import com.quranengine.domain.quranaudiokit.QuranAudioPlayer
import com.quranengine.domain.quranaudiokit.QuranAudioRequestBuilder
import com.quranengine.domain.quranaudiokit.QueuingPlayer
import com.quranengine.domain.audiotimingservice.ReciterTimingRetriever
import com.quranengine.domain.qurantextkit.AyahShareUseCase
import com.quranengine.domain.qurantextkit.CompositeSearcher
import com.quranengine.domain.qurantextkit.FontSizePreferences
import com.quranengine.domain.qurantextkit.QuranTextDataService
import com.quranengine.domain.qurantextkit.QuranContentStatePreferences
import com.quranengine.domain.qurantextkit.SearchRecentsService
import com.quranengine.domain.qurantextkit.SearchTerm
import com.quranengine.domain.qurantextkit.Searcher
import com.quranengine.domain.qurantextkit.localizedName
import com.quranengine.domain.readingservice.ReadingAssetsInstaller
import com.quranengine.domain.readingservice.ReadingPreferences
import com.quranengine.domain.reciterservice.ReciterDataRetriever
import com.quranengine.domain.reciterservice.ReciterDataSource
import com.quranengine.domain.reciterservice.AudioUnzipper
import com.quranengine.domain.reciterservice.DownloadedRecitersService
import com.quranengine.domain.reciterservice.RecentRecitersService
import com.quranengine.domain.reciterservice.ReciterAudioDeleter
import com.quranengine.domain.reciterservice.ReciterPreferences
import com.quranengine.domain.settingsservice.ReviewPersistence
import com.quranengine.domain.translationservice.DefaultTranslationUnzipper
import com.quranengine.domain.translationservice.LocalTranslationsRetriever
import com.quranengine.domain.translationservice.SelectedTranslationsPreferences
import com.quranengine.domain.translationservice.TranslationUnzipper
import com.quranengine.domain.translationservice.TranslationDeleter
import com.quranengine.domain.translationservice.TranslationNetworkManager
import com.quranengine.domain.translationservice.TranslationsDownloader
import com.quranengine.domain.translationservice.TranslationsRepository
import com.quranengine.domain.translationservice.TranslationsVersionUpdater
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurantext.SearchResults
import com.quranengine.model.qurantext.Translation
import com.quranengine.ui.theme.ThemePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Hilt module providing domain-layer services.
 *
 * Services here depend on core abstractions (preferences, file system) and
 * data-layer persistence, but never on the UI layer.
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    /** Application-wide [CoroutineScope] tied to the process lifetime. */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // -- Preferences-based services -------------------------------------------

    @Provides
    @Singleton
    fun provideAudioPreferences(preferences: Preferences): AudioPreferences =
        AudioPreferences(preferences)

    @Provides
    @Singleton
    fun provideFontSizePreferences(preferences: Preferences): FontSizePreferences =
        FontSizePreferences(preferences)

    @Provides
    @Singleton
    fun provideQuranContentStatePreferences(preferences: Preferences): QuranContentStatePreferences =
        QuranContentStatePreferences(preferences)

    @Provides
    @Singleton
    fun provideReadingPreferences(preferences: Preferences): ReadingPreferences =
        ReadingPreferences(preferences)

    @Provides
    @Singleton
    fun provideReadingAssetsInstaller(
        systemBundle: SystemBundle,
        fileSystem: FileSystem,
        @Named("baseDir") baseDir: File,
    ): ReadingAssetsInstaller =
        ReadingAssetsInstaller(systemBundle, fileSystem, baseDir)

    @Provides
    @Singleton
    fun provideReciterPreferences(preferences: Preferences): ReciterPreferences =
        ReciterPreferences(preferences)

    @Provides
    @Singleton
    fun provideSelectedTranslationsPreferences(preferences: Preferences): SelectedTranslationsPreferences =
        SelectedTranslationsPreferences(preferences)

    @Provides
    @Singleton
    fun provideSearchRecentsService(preferences: Preferences): SearchRecentsService =
        SearchRecentsService(preferences)

    @Provides
    @Singleton
    fun provideReviewPersistence(preferences: Preferences): ReviewPersistence =
        ReviewPersistence(preferences)

    @Provides
    @Singleton
    fun provideThemePreferences(preferences: Preferences): ThemePreferences =
        ThemePreferences(preferences)

    // -- Annotation services --------------------------------------------------

    @Provides
    @Singleton
    fun providePageBookmarkService(persistence: PageBookmarkPersistence): PageBookmarkService =
        PageBookmarkService(persistence)

    @Provides
    @Singleton
    fun provideLastPageService(persistence: LastPagePersistence): LastPageService =
        LastPageService(persistence)

    @Provides
    @Singleton
    fun provideLastPageUpdater(service: LastPageService, scope: CoroutineScope): LastPageUpdater =
        LastPageUpdater(service, scope)

    @Provides
    @Singleton
    fun provideAnnotationAnalytics(): com.quranengine.domain.annotationservice.AnalyticsLibrary =
        object : com.quranengine.domain.annotationservice.AnalyticsLibrary {
            override fun logEvent(name: String, value: String) = Unit
        }

    @Provides
    @Singleton
    fun provideNoteService(
        persistence: NotePersistence,
        analytics: com.quranengine.domain.annotationservice.AnalyticsLibrary,
        preferences: Preferences,
    ): NoteService =
        NoteService(persistence, analytics, preferences)

    // -- Translation services -------------------------------------------------

    @Provides
    @Singleton
    fun provideTranslationNetworkManager(networkManager: NetworkManager): TranslationNetworkManager =
        TranslationNetworkManager(networkManager)

    @Provides
    @Singleton
    fun provideTranslationsRepository(
        translationNetworkManager: TranslationNetworkManager,
        persistence: SqliteActiveTranslationsPersistence,
    ): TranslationsRepository =
        TranslationsRepository(translationNetworkManager, persistence)

    @Provides
    @Singleton
    fun provideTranslationUnzipper(
        zipper: Zipper,
        fileSystem: FileSystem,
    ): TranslationUnzipper = DefaultTranslationUnzipper(zipper, fileSystem)

    @Provides
    @Singleton
    fun provideTranslationsVersionUpdater(
        persistence: SqliteActiveTranslationsPersistence,
        translationUnzipper: TranslationUnzipper,
        fileSystem: FileSystem,
        selectedTranslationsPreferences: SelectedTranslationsPreferences,
        @Named("baseDir") baseDir: File,
    ): TranslationsVersionUpdater =
        TranslationsVersionUpdater(
            persistence = persistence,
            versionPersistenceFactory = { translation ->
                val dbPath = File(baseDir, translation.localPath).absolutePath
                SqliteDatabaseVersionPersistence(ReadOnlyDatabase.openPath(dbPath))
            },
            unzipper = translationUnzipper,
            fileSystem = fileSystem,
            selectedTranslationsPreferences = selectedTranslationsPreferences,
            baseDir = baseDir,
        )

    @Provides
    @Singleton
    fun provideLocalTranslationsRetriever(
        persistence: SqliteActiveTranslationsPersistence,
        versionUpdater: TranslationsVersionUpdater,
    ): LocalTranslationsRetriever =
        LocalTranslationsRetriever(persistence, versionUpdater)

    @Provides
    @Singleton
    fun provideTranslationsDownloader(
        downloadManager: DownloadManager,
    ): TranslationsDownloader =
        TranslationsDownloader(downloadManager)

    @Provides
    @Singleton
    fun provideTranslationDeleter(
        persistence: SqliteActiveTranslationsPersistence,
        selectedTranslationsPreferences: SelectedTranslationsPreferences,
        fileSystem: FileSystem,
        @Named("baseDir") baseDir: File,
    ): TranslationDeleter =
        TranslationDeleter(persistence, selectedTranslationsPreferences, fileSystem, baseDir)

    // -- Reciter services -----------------------------------------------------

    @Provides
    @Singleton
    fun provideRecentRecitersService(reciterPreferences: ReciterPreferences): RecentRecitersService =
        RecentRecitersService(reciterPreferences)

    @Provides
    @Singleton
    fun provideReciterDataSource(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): ReciterDataSource =
        ReciterDataSource { context.assets.open("reciters.json") }

    @Provides
    @Singleton
    fun provideReciterDataRetriever(
        dataSource: ReciterDataSource,
        fileSystem: FileSystem,
        localizer: Localizer,
        @Named("baseDir") baseDir: File,
    ): ReciterDataRetriever =
        ReciterDataRetriever(dataSource, fileSystem, baseDir, localizer)

    @Provides
    @Singleton
    fun provideDownloadedRecitersService(
        fileSystem: FileSystem,
        @Named("baseDir") baseDir: File,
    ): DownloadedRecitersService =
        DownloadedRecitersService(fileSystem, baseDir)

    @Provides
    @Singleton
    fun provideAudioUnzipper(
        zipper: Zipper,
        fileSystem: FileSystem,
        @Named("baseDir") baseDir: File,
    ): AudioUnzipper =
        AudioUnzipper(zipper, fileSystem, baseDir)

    @Provides
    @Singleton
    fun provideReciterAudioDeleter(
        fileSystem: FileSystem,
        @Named("baseDir") baseDir: File,
    ): ReciterAudioDeleter =
        ReciterAudioDeleter(fileSystem, baseDir)

    @Provides
    @Singleton
    fun providePreferencesLastAyahFinder(audioPreferences: AudioPreferences): PreferencesLastAyahFinder =
        PreferencesLastAyahFinder(audioPreferences)

    @Provides
    @Singleton
    fun provideQueuingPlayer(queuePlayer: QueuePlayer): QueuingPlayer =
        object : QueuingPlayer {
            override var actions
                get() = queuePlayer.actions
                set(value) {
                    queuePlayer.actions = value
                }

            override fun play(
                request: com.quranengine.core.audioplayer.AudioRequest,
                rate: Float,
            ) {
                queuePlayer.play(request, rate)
            }

            override fun pause() {
                queuePlayer.pause()
            }

            override fun resume() {
                queuePlayer.resume()
            }

            override fun stop() {
                queuePlayer.stop()
            }

            override fun stepForward() {
                queuePlayer.stepForward()
            }

            override fun stepBackward() {
                queuePlayer.stepBackward()
            }

            override fun setRate(rate: Float) {
                queuePlayer.setRate(rate)
            }
        }

    @Provides
    @Singleton
    @Named("filesAppHost")
    fun provideFilesAppHost(): String = "https://files.quran.app/"

    @Provides
    @Singleton
    fun provideReciterTimingRetriever(@Named("baseDir") baseDir: File): ReciterTimingRetriever =
        ReciterTimingRetriever { relativePath ->
            val db = ReadOnlyDatabase.openPath(File(baseDir, relativePath).absolutePath)
            SqliteAyahTimingPersistence(db)
        }

    @Provides
    @Singleton
    @Named("gapless")
    fun provideGaplessAudioRequestBuilder(
        timingRetriever: ReciterTimingRetriever,
        localizer: Localizer,
        @Named("baseDir") baseDir: File,
    ): QuranAudioRequestBuilder =
        GaplessAudioRequestBuilder(timingRetriever, localizer, baseDir)

    @Provides
    @Singleton
    fun provideQuranAudioDownloader(
        @Named("filesAppHost") filesAppHost: String,
        downloadManager: DownloadManager,
        fileSystem: FileSystem,
        @Named("baseDir") baseDir: File,
    ): QuranAudioDownloader =
        QuranAudioDownloader(filesAppHost, downloadManager, fileSystem, baseDir)

    @Provides
    @Singleton
    fun provideQuranAudioPlayer(
        player: QueuingPlayer,
        unzipper: AudioUnzipper,
        nowPlayingUpdater: NowPlayingUpdater,
        @Named("gapless") gaplessBuilder: QuranAudioRequestBuilder,
        @Named("gapped") gappedBuilder: QuranAudioRequestBuilder,
    ): QuranAudioPlayer =
        QuranAudioPlayer(player, unzipper, nowPlayingUpdater, gaplessBuilder, gappedBuilder)

    // -- Search ---------------------------------------------------------------

    @Provides
    @Singleton
    fun provideVerseTextPersistence(
        @Named("databasesDir") databasesDir: File,
    ): VerseTextPersistence {
        val dbPath = File(databasesDir, "quran.ar.uthmani.v2.db").absolutePath
        return SqliteVerseTextPersistence(ReadOnlyDatabase.openPath(dbPath))
    }

    @Provides
    @Singleton
    fun provideQuranTextDataService(
        localTranslationsRetriever: LocalTranslationsRetriever,
        verseTextPersistence: VerseTextPersistence,
        selectedTranslationsPreferences: SelectedTranslationsPreferences,
        localizer: Localizer,
        @Named("baseDir") baseDir: File,
    ): QuranTextDataService =
        QuranTextDataService(
            localTranslationRetriever = localTranslationsRetriever,
            arabicPersistence = verseTextPersistence,
            translationsPersistenceBuilder = { translation ->
                val dbPath = File(baseDir, translation.localPath).absolutePath
                SqliteTranslationVerseTextPersistence(
                    db = ReadOnlyDatabase.openPath(dbPath),
                    fileName = translation.fileName,
                )
            },
            selectedTranslationsPreferences = selectedTranslationsPreferences,
            localizer = localizer,
        )

    @Provides
    @Singleton
    fun provideAyahShareUseCase(
        @ApplicationContext context: Context,
        quranTextDataService: QuranTextDataService,
        selectedTranslationsPreferences: SelectedTranslationsPreferences,
        localizer: Localizer,
    ): AyahShareUseCase =
        AyahShareUseCase(
            context = context,
            quranTextDataService = quranTextDataService,
            selectedTranslationsPreferences = selectedTranslationsPreferences,
            localizer = localizer,
        )

    @Provides
    @Singleton
    fun provideSearcher(
        quran: Quran,
        localizer: Localizer,
        verseTextPersistence: VerseTextPersistence,
        localTranslationRetriever: LocalTranslationsRetriever,
        @Named("baseDir") baseDir: File,
    ): Searcher {
        val composite = CompositeSearcher(
            quranVerseTextPersistence = verseTextPersistence,
            localTranslationRetriever = localTranslationRetriever,
            versePersistenceBuilder = { translation: Translation ->
                val dbPath = File(baseDir, translation.localPath).absolutePath
                SqliteTranslationVerseTextPersistence(
                    db = ReadOnlyDatabase.openPath(dbPath),
                    fileName = translation.fileName,
                )
            },
            localizedSuraName = { suraNumber, withPrefix, language ->
                val localizedLanguage = when (language) {
                    "ar" -> com.quranengine.core.localization.Language.ARABIC
                    "en", null -> com.quranengine.core.localization.Language.ENGLISH.takeIf { language == "en" }
                    else -> null
                }
                quran.suras.first { it.suraNumber == suraNumber }
                    .localizedName(localizer, withPrefix = withPrefix, language = localizedLanguage)
            },
            localizedJuzName = { juzNumber ->
                quran.juzs.first { it.juzNumber == juzNumber }.localizedName(localizer)
            },
            localizedHizbName = { hizbNumber ->
                quran.hizbs.first { it.hizbNumber == hizbNumber }.localizedName(localizer)
            },
            localizedPageName = { pageNumber ->
                quran.pages.first { it.pageNumber == pageNumber }.localizedName(localizer)
            },
        )

        return object : Searcher {
            override suspend fun autocomplete(term: SearchTerm, quran: Quran): List<String> =
                composite.autocomplete(term.compactQuery, quran)

            override suspend fun search(term: SearchTerm, quran: Quran): List<SearchResults> =
                composite.search(term.compactQuery, quran)
        }
    }

    // -- Audio request builders -----------------------------------------------

    @Provides
    @Singleton
    @Named("gapped")
    fun provideGappedAudioRequestBuilder(
        localizer: Localizer,
        @Named("baseDir") baseDir: File,
    ): QuranAudioRequestBuilder =
        GappedAudioRequestBuilder(localizer, baseDir)
}
