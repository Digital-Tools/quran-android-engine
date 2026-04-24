package com.quranengine.model.quranaudio

import com.quranengine.model.qurankit.Sura

data class RangeTiming(
    val timings: Map<Sura, SuraTiming>,
    val endTime: Timing?
)
