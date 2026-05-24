package com.quranengine.features.quranview

import com.quranengine.model.qurankit.AyahNumber

data class AyahMenuActions(
    val onBookmarkPage: (AyahNumber) -> Unit = {},
    val onPlayFromHere: (AyahNumber) -> Unit = {},
    val onShare: (AyahNumber) -> Unit = {},
    val onCopy: (AyahNumber) -> Unit = {},
    val onAddNote: (AyahNumber) -> Unit = {},
    val onToggleTranslations: () -> Unit = {},
    val onManageTranslations: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
