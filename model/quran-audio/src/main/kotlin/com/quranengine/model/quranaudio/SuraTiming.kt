package com.quranengine.model.quranaudio

data class SuraTiming(
    val verses: List<AyahTiming>,
    val endTime: Timing?
)
