package com.quranengine.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

enum class QuranFontSize {
    X_SMALL, SMALL, MEDIUM, LARGE, X_LARGE, XX_LARGE, XXX_LARGE,
    ACCESSIBILITY1, ACCESSIBILITY2, ACCESSIBILITY3, ACCESSIBILITY4, ACCESSIBILITY5;

    fun scaledSize(mediumSize: Float): Float {
        val factor = when (this) {
            X_SMALL -> 0.7f * 0.7f * 0.7f
            SMALL -> 0.7f * 0.7f
            MEDIUM -> 0.7f
            LARGE -> 1f
            X_LARGE -> 1f / 0.8f
            XX_LARGE -> 1f / 0.8f / 0.8f
            XXX_LARGE -> 1f / 0.8f / 0.8f / 0.8f
            ACCESSIBILITY1 -> 1f / 0.8f / 0.8f / 0.8f / 0.8f
            ACCESSIBILITY2 -> 1f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f
            ACCESSIBILITY3 -> 1f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f
            ACCESSIBILITY4 -> 1f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f
            ACCESSIBILITY5 -> 1f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f / 0.8f
        }
        return mediumSize * factor
    }

    fun quranTextSize(): TextUnit = scaledSize(QURAN_TEXT_MEDIUM_SIZE).sp

    fun arabicTafseerTextSize(): TextUnit = scaledSize(ARABIC_TAFSEER_MEDIUM_SIZE).sp

    companion object {
        private const val QURAN_TEXT_MEDIUM_SIZE = 21f
        private const val ARABIC_TAFSEER_MEDIUM_SIZE = 21f
    }
}

// Font families - these reference custom font resources that would be bundled with the app
// For now, define placeholders that can be replaced with actual font resources
object QuranFontFamilies {
    // For Quran Arabic text (e.g., "me_quran" font)
    val quranText: FontFamily = FontFamily.Default

    // For Arabic tafseer text
    val arabicTafseer: FontFamily = FontFamily.Default

    // For decorated sura names
    val suraNames: FontFamily = FontFamily.Default
}
