package com.quranengine.model.quranaudio

import com.quranengine.model.qurankit.AyahNumber

data class AyahTiming(
    val ayah: AyahNumber,
    val time: Timing
)
