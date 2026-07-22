package com.quranengine.domain.translationservice

import com.quranengine.domain.readingservice.VerseTextAssetsInstaller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Installs bundled Arabic verse text and translation databases once per process,
 * then exposes [ready] so reader screens can reload after bootstrap completes.
 */
class QuranContentBootstrap(
    private val verseTextAssetsInstaller: VerseTextAssetsInstaller,
    private val translationAssetsInstaller: TranslationAssetsInstaller,
    private val applicationScope: CoroutineScope,
) {
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val started = AtomicBoolean(false)

    fun start() {
        if (!started.compareAndSet(false, true)) return

        applicationScope.launch {
            try {
                verseTextAssetsInstaller.ensureInstalled()
                translationAssetsInstaller.ensureInstalled()
            } catch (error: Exception) {
                Timber.e(error, "QuranContentBootstrap failed")
            } finally {
                _ready.value = true
            }
        }
    }
}
