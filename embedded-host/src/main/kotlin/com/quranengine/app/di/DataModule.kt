package com.quranengine.app.di

import android.content.Context
import androidx.room.Room
import com.quranengine.core.system.FileSystem
import com.quranengine.data.annotation.QuranAnnotationsDatabase
import com.quranengine.data.annotation.dao.LastPageDao
import com.quranengine.data.annotation.dao.NoteDao
import com.quranengine.data.annotation.dao.PageBookmarkDao
import com.quranengine.data.annotation.persistence.LastPagePersistence
import com.quranengine.data.annotation.persistence.NotePersistence
import com.quranengine.data.annotation.persistence.PageBookmarkPersistence
import com.quranengine.data.annotation.persistence.RoomLastPagePersistence
import com.quranengine.data.annotation.persistence.RoomNotePersistence
import com.quranengine.data.annotation.persistence.RoomPageBookmarkPersistence
import com.quranengine.data.batchdownloader.DownloadManager
import com.quranengine.data.batchdownloader.db.DownloadsDatabase
import com.quranengine.data.network.NetworkManager
import com.quranengine.data.translation.SqliteActiveTranslationsPersistence
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.io.File
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

/**
 * Hilt module providing the data layer: Room databases, DAOs,
 * persistence implementations, and network clients.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    // -- Room databases -------------------------------------------------------

    @Provides
    @Singleton
    fun provideAnnotationsDatabase(@ApplicationContext context: Context): QuranAnnotationsDatabase =
        Room.databaseBuilder(context, QuranAnnotationsDatabase::class.java, "quran_annotations.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDownloadsDatabase(@ApplicationContext context: Context): DownloadsDatabase =
        Room.databaseBuilder(context, DownloadsDatabase::class.java, "downloads.db")
            .fallbackToDestructiveMigration()
            .build()

    // -- DAOs -----------------------------------------------------------------

    @Provides
    fun providePageBookmarkDao(db: QuranAnnotationsDatabase): PageBookmarkDao =
        db.pageBookmarkDao()

    @Provides
    fun provideLastPageDao(db: QuranAnnotationsDatabase): LastPageDao =
        db.lastPageDao()

    @Provides
    fun provideNoteDao(db: QuranAnnotationsDatabase): NoteDao =
        db.noteDao()

    // -- Persistence implementations ------------------------------------------

    @Provides
    @Singleton
    fun providePageBookmarkPersistence(dao: PageBookmarkDao): PageBookmarkPersistence =
        RoomPageBookmarkPersistence(dao)

    @Provides
    @Singleton
    fun provideLastPagePersistence(dao: LastPageDao): LastPagePersistence =
        RoomLastPagePersistence(dao)

    @Provides
    @Singleton
    fun provideNotePersistence(dao: NoteDao): NotePersistence =
        RoomNotePersistence(dao)

    @Provides
    @Singleton
    fun provideActiveTranslationsPersistence(
        @ApplicationContext context: Context,
        @Named("databasesDir") databasesDir: File,
    ): SqliteActiveTranslationsPersistence =
        SqliteActiveTranslationsPersistence(context, databasesDir.absolutePath)

    // -- Network --------------------------------------------------------------

    @Provides
    @Singleton
    fun provideHttpClient(okHttpClient: OkHttpClient): HttpClient =
        HttpClient(OkHttp) {
            engine {
                preconfigured = okHttpClient
            }
        }

    @Provides
    @Singleton
    fun provideNetworkManager(httpClient: HttpClient): NetworkManager =
        NetworkManager(httpClient, "https://quran.app")

    // -- Download Manager -----------------------------------------------------

    /**
     * Uses the secondary constructor of [DownloadManager] that accepts
     * [DownloadsDatabase] directly, side-stepping the `internal` visibility
     * of `DownloadsPersistence`.
     */
    @Provides
    @Singleton
    fun provideDownloadManager(
        okHttpClient: OkHttpClient,
        fileSystem: FileSystem,
        @Named("baseDir") baseDir: File,
        downloadsDatabase: DownloadsDatabase,
        applicationScope: CoroutineScope,
    ): DownloadManager =
        DownloadManager(
            maxSimultaneousDownloads = 3,
            okHttpClient = okHttpClient,
            database = downloadsDatabase,
            fileSystem = fileSystem,
            baseDir = baseDir,
        ).also { manager ->
            applicationScope.launch {
                manager.start()
            }
        }
}
