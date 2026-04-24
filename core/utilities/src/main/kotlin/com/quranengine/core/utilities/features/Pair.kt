package com.quranengine.core.utilities.features

/**
 * A simple generic pair. Kotlin already has [kotlin.Pair], but this provides
 * a named data class matching the Swift port for domain clarity.
 */
data class Pair<A, B>(
    val first: A,
    val second: B,
)
