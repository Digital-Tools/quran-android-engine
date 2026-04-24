package com.quranengine.core.audioplayer

/**
 * Represents the number of times a frame or request should repeat.
 * Port of Swift Runs enum.
 */
enum class Runs(val maxRuns: Int) {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    INDEFINITE(Int.MAX_VALUE),
}
