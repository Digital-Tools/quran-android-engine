package com.quranengine.features.quranview

import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurantext.QuranMode
import com.quranengine.ui.audiobanner.AudioBannerState

data class QuranViewState(
    val currentPage: Int = 1,
    val totalPages: Int = 604,
    val visiblePages: List<Int> = listOf(1),
    val isCurrentPageBookmarked: Boolean = false,
    val barsVisible: Boolean = true,
    val quranMode: QuranMode = QuranMode.ARABIC,
    val twoPagesEnabled: Boolean = false,
    val audioBannerState: AudioBannerState = AudioBannerState(),
    val title: String = "",
    val subtitle: String = "",
    val firstVerse: AyahNumber? = null,
    val lastVerse: AyahNumber? = null,
)
