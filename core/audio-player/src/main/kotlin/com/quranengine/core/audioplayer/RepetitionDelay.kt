package com.quranengine.core.audioplayer

/**
 * A fixed pause before the whole set of verses starts again.
 * Port of iOS `RepetitionDelay`; [id] matches its raw value, so only append.
 */
enum class RepetitionDelay(val id: Int, val seconds: Double) {
    NONE(0, 0.0),
    ONE_SECOND(1, 1.0),
    TWO_SECONDS(2, 2.0),
    THREE_SECONDS(3, 3.0),
    FIVE_SECONDS(4, 5.0),
    TEN_SECONDS(5, 10.0),
    ;

    companion object {
        fun fromId(id: Int): RepetitionDelay? = entries.firstOrNull { it.id == id }
    }
}
