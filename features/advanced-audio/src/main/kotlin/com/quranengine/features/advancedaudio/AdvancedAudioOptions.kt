package com.quranengine.features.advancedaudio

import com.quranengine.core.audioplayer.Runs
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.qurankit.AyahNumber

data class AdvancedAudioOptions(
    val reciter: Reciter,
    val start: AyahNumber,
    val end: AyahNumber,
    val verseRuns: Runs,
    val listRuns: Runs,
    val playbackRate: Float,
)
