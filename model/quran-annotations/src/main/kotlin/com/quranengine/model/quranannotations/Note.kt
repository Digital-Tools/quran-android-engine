package com.quranengine.model.quranannotations

import com.quranengine.model.qurankit.AyahNumber
import java.time.Instant

data class Note(
    val verses: Set<AyahNumber>,
    val modifiedDate: Instant,
    val color: Color,
    val note: String?
) {
    enum class Color(val value: Int) {
        RED(0),
        GREEN(1),
        BLUE(2),
        YELLOW(3),
        PURPLE(4);

        companion object {
            fun fromValue(value: Int): Color =
                entries.first { it.value == value }
        }
    }

    val firstVerse: AyahNumber
        get() = verses.sorted().first()
}
