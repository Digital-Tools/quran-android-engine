package com.quranengine.embeddedhost

import android.content.Context
import com.quranengine.domain.readingservice.ReadingResourcesService
import com.quranengine.domain.translationservice.QuranContentBootstrap
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuranContentEntryPoint {
    fun readingResourcesService(): ReadingResourcesService
    fun quranContentBootstrap(): QuranContentBootstrap
    fun applicationScope(): CoroutineScope
}

/**
 * Starts downloading the selected mushaf's pages and the default
 * translations when the host app launches (neither is bundled), so they are
 * usually there before the reader is first opened. Only the selected
 * mushaf; others download when picked. Safe to call more than once.
 */
object QuranContentPrefetch {
    fun start(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            QuranContentEntryPoint::class.java,
        )
        entryPoint.quranContentBootstrap().start()
        entryPoint.applicationScope().launch {
            entryPoint.readingResourcesService().startLoadingResources()
        }
    }
}
