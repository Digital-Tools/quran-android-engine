package com.quranengine.core.utilities.extensions

/**
 * Removes consecutive duplicate elements from the list.
 * Only adjacent duplicates are removed, preserving the first occurrence in each run.
 */
fun <T> List<T>.removingNeighboringDuplicates(): List<T> {
    if (isEmpty()) return emptyList()
    return buildList {
        for (value in this@removingNeighboringDuplicates) {
            if (value != lastOrNull()) add(value)
        }
    }
}

/**
 * Sorts this list according to the order defined by [order].
 * Elements whose key is not found in [order] are placed at the end.
 */
inline fun <T, K> List<T>.sortedAs(order: List<K>, crossinline keySelector: (T) -> K): List<T> {
    val indices = order.withIndex().associate { (index, value) -> value to index }
    return sortedBy { indices[keySelector(it)] ?: Int.MAX_VALUE }
}
