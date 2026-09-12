package com.quranengine.features.quranview

import com.quranengine.model.quranannotations.Note
import com.quranengine.model.qurankit.AyahNumber

data class AyahMenuActions(
    val onPlayFromHere: (List<AyahNumber>) -> Unit = {},
    val onRepeatVerse: (List<AyahNumber>) -> Unit = {},
    val onHighlight: (List<AyahNumber>) -> Unit = {},
    val onSelectHighlightColor: (List<AyahNumber>, Note.Color) -> Unit = { _, _ -> },
    val onAddNote: (List<AyahNumber>) -> Unit = {},
    val onDeleteNote: (List<AyahNumber>) -> Unit = {},
    val onTranslationTafseer: (List<AyahNumber>) -> Unit = {},
    val onCopy: (List<AyahNumber>) -> Unit = {},
    val onShare: (List<AyahNumber>) -> Unit = {},
    val onDismiss: () -> Unit = {},
)
