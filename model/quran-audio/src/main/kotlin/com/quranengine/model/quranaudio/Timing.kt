package com.quranengine.model.quranaudio

data class Timing(val time: Int) {
    val seconds: Double get() = time / 1000.0
}
