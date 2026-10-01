package com.quranengine.core.audioplayer

/**
 * Represents the number of times a frame or request should repeat.
 * Port of Swift Runs enum. Any finite count is allowed (custom repeats in
 * advanced audio options); the presets below are just common values.
 */
class Runs private constructor(val maxRuns: Int) {
    override fun equals(other: Any?): Boolean = other is Runs && other.maxRuns == maxRuns

    override fun hashCode(): Int = maxRuns

    override fun toString(): String = if (this == INDEFINITE) "Runs.INDEFINITE" else "Runs($maxRuns)"

    companion object {
        val ONE = Runs(1)
        val TWO = Runs(2)
        val THREE = Runs(3)
        val FOUR = Runs(4)
        val INDEFINITE = Runs(Int.MAX_VALUE)

        /** Largest count offered by the custom repeat control. */
        const val MAX_CUSTOM = 999

        /** Runs for [count] plays; counts below 1 play once, [Int.MAX_VALUE] loops. */
        fun of(count: Int): Runs = Runs(count.coerceAtLeast(1))
    }
}
