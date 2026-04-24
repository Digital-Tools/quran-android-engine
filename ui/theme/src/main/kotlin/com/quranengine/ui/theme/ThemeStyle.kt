package com.quranengine.ui.theme

enum class ThemeStyle {
    CALM, FOCUS, ORIGINAL, PAPER, QUIET;

    companion object {
        val styles: List<ThemeStyle> = listOf(PAPER, ORIGINAL, QUIET, CALM, FOCUS)
        val defaultStyle: ThemeStyle = PAPER
    }
}
