package com.quranengine.features.quranview

import com.quranengine.model.qurankit.AyahNumber

data class AyahMenuActions(
    val onPlayFromHere: (AyahNumber) -> Unit = {},
    val onRepeatVerse: (AyahNumber) -> Unit = {},
    val onHighlight: (AyahNumber) -> Unit = {},
    val onSelectHighlightColor: (AyahNumber) -> Unit = {},
    val onAddNote: (AyahNumber) -> Unit = {},
    val onTranslationTafseer: (AyahNumber) -> Unit = {},
    val onCopy: (AyahNumber) -> Unit = {},
    val onShare: (AyahNumber) -> Unit = {},
    val onDismiss: () -> Unit = {},
)
