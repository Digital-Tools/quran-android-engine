package com.quranengine.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.quranengine.core.audioplayer.QueuePlayer
import com.quranengine.core.audioplayer.NowPlayingUpdater
import com.quranengine.core.localization.Localizer
import com.quranengine.core.localization.MapLocalizer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.system.DefaultEventObserver
import com.quranengine.core.system.DefaultFileSystem
import com.quranengine.core.system.DefaultSecureStorage
import com.quranengine.core.system.DefaultSystemBundle
import com.quranengine.core.system.DefaultSystemTime
import com.quranengine.core.system.DefaultZipper
import com.quranengine.core.system.EventObserver
import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.SecureStorage
import com.quranengine.core.system.SystemBundle
import com.quranengine.core.system.SystemTime
import com.quranengine.core.system.Zipper
import com.quranengine.model.qurankit.Quran
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient

/**
 * Hilt module providing core/system-level singletons.
 *
 * These are low-level dependencies shared across every layer of the app:
 * preferences, file system abstractions, localization, networking, and
 * the canonical [Quran] model instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("quran_engine_prefs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun providePreferences(sharedPreferences: SharedPreferences): Preferences =
        Preferences(sharedPreferences)

    // -- System abstractions --------------------------------------------------

    @Provides
    @Singleton
    fun provideFileSystem(): FileSystem = DefaultFileSystem()

    @Provides
    @Singleton
    fun provideSystemTime(): SystemTime = DefaultSystemTime()

    @Provides
    @Singleton
    fun provideSecureStorage(@ApplicationContext context: Context): SecureStorage =
        DefaultSecureStorage(context)

    @Provides
    @Singleton
    fun provideEventObserver(): EventObserver = DefaultEventObserver()

    @Provides
    @Singleton
    fun provideSystemBundle(@ApplicationContext context: Context): SystemBundle =
        DefaultSystemBundle(context)

    @Provides
    @Singleton
    fun provideZipper(): Zipper = DefaultZipper()

    // -- Localization ---------------------------------------------------------

    @Provides
    @Singleton
    fun provideLocalizer(): Localizer = MapLocalizer()

    // -- Model ----------------------------------------------------------------

    @Provides
    @Singleton
    fun provideQuran(): Quran = Quran.hafsMadani1405

    // -- Directories ----------------------------------------------------------

    @Provides
    @Singleton
    @Named("baseDir")
    fun provideBaseDir(@ApplicationContext context: Context): File = context.filesDir

    @Provides
    @Singleton
    @Named("databasesDir")
    fun provideDatabasesDir(@ApplicationContext context: Context): File =
        File(context.filesDir, "databases")

    // -- Networking -----------------------------------------------------------

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    // -- Audio ----------------------------------------------------------------

    @Provides
    @Singleton
    fun provideQueuePlayer(@ApplicationContext context: Context): QueuePlayer = QueuePlayer(context)

    @Provides
    @Singleton
    fun provideNowPlayingPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context).build()

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        nowPlayingPlayer: ExoPlayer,
    ): MediaSession = MediaSession.Builder(context, nowPlayingPlayer).build()

    @Provides
    @Singleton
    fun provideNowPlayingUpdater(mediaSession: MediaSession): NowPlayingUpdater =
        NowPlayingUpdater(mediaSession)
}
