package com.quranengine.core.utilities.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

/**
 * Collects all emitted values from this flow into a list.
 * Equivalent to Swift's `AsyncSequence.collect()`.
 */
suspend fun <T> Flow<T>.collectAsList(): List<T> = toList()

/**
 * Collects flow values, applying [transform] to each and returning the results.
 */
suspend fun <T, R> Flow<T>.collectAsListMap(transform: suspend (T) -> R): List<R> {
    val result = mutableListOf<R>()
    collect { result.add(transform(it)) }
    return result
}
