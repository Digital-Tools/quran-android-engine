package com.quranengine.domain.readingservice

import com.quranengine.model.qurankit.Reading

/** User-facing name for a [Reading] — the enum itself carries no display metadata. */
val Reading.displayTitle: String
    get() = when (this) {
        Reading.HAFS_1405 -> "Hafs (1405 AH)"
        Reading.HAFS_1421 -> "Hafs (1421 AH)"
        Reading.HAFS_1440 -> "Hafs (1440 AH)"
        Reading.TAJWEED -> "Tajweed"
    }

/** Short one-line description shown under the title in the mushaf picker. */
val Reading.displaySubtitle: String
    get() = when (this) {
        Reading.HAFS_1405 -> "The standard Madani mushaf"
        Reading.HAFS_1421 -> "An earlier Madani print"
        Reading.HAFS_1440 -> "A more recent Madani print"
        Reading.TAJWEED -> "Color-coded tajweed rules"
    }
