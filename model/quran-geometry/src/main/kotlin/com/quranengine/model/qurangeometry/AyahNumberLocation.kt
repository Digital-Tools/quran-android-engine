package com.quranengine.model.qurangeometry

import com.quranengine.model.qurankit.AyahNumber

data class AyahNumberLocation(
    val ayah: AyahNumber,
    val x: Int,
    val y: Int
)
