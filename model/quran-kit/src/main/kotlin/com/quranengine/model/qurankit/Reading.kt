package com.quranengine.model.qurankit

enum class Reading(val value: Int) {
    HAFS_1405(0),
    HAFS_1440(1),
    TAJWEED(2),
    HAFS_1421(3);

    val quran: Quran
        get() = when (this) {
            HAFS_1405 -> Quran.hafsMadani1405
            HAFS_1440 -> Quran.hafsMadani1440
            HAFS_1421 -> Quran.hafsMadani1440
            TAJWEED -> Quran.hafsMadani1405
        }

    companion object {
        val sortedReadings: List<Reading> = listOf(HAFS_1405, TAJWEED, HAFS_1421, HAFS_1440)

        fun fromValue(value: Int): Reading? = entries.firstOrNull { it.value == value }
    }
}
