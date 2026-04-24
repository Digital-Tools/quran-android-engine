package com.quranengine.model.qurantext

enum class FontSize(val rawValue: Int) {
    ACCESSIBILITY_5(-6),
    ACCESSIBILITY_4(-5),
    ACCESSIBILITY_3(-4),
    ACCESSIBILITY_2(-3),
    ACCESSIBILITY_1(-2),
    XXX_LARGE(-1),
    XX_LARGE(0),
    X_LARGE(1),
    LARGE(2),
    MEDIUM(3),
    SMALL(4),
    X_SMALL(5);

    /** Scale factor relative to the default (XX_LARGE / 0). Higher rawValue → smaller text. */
    val scaleSp: Float
        get() = 1.0f - (rawValue * 0.08f)

    companion object {
        fun fromRawValue(rawValue: Int): FontSize? =
            entries.firstOrNull { it.rawValue == rawValue }
    }

    fun distanceTo(other: FontSize): Int =
        other.rawValue - rawValue

    fun advancedBy(n: Int): FontSize? =
        fromRawValue(rawValue + n)

    private fun fromRawValue(value: Int): FontSize? =
        Companion.fromRawValue(value)
}
