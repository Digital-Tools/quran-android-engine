package com.quranengine.core.audioplayer

/**
 * A pause after each verse, as a multiple of how long that verse took to
 * recite: with [HALF], an 8 s verse is followed by 4 s of silence. Applies
 * between every verse, including repeats of the same verse.
 * Port of iOS `VerseDelay`; [id] matches its raw value, so only append.
 */
enum class VerseDelay(val id: Int, val multiplier: Double) {
    NONE(0, 0.0),
    QUARTER(1, 0.25),
    HALF(2, 0.5),
    THREE_QUARTERS(3, 0.75),
    FULL(4, 1.0),
    DOUBLE(5, 2.0),
    ;

    companion object {
        fun fromId(id: Int): VerseDelay? = entries.firstOrNull { it.id == id }
    }
}
