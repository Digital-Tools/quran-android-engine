package com.quranengine.core.utilities.extensions

/**
 * Groups elements by a key, keeping only the last element for each key.
 * Unlike `groupBy`, this produces a flat map of key → single element.
 */
inline fun <T, K> Iterable<T>.flatGroup(keySelector: (T) -> K): Map<K, T> {
    val result = LinkedHashMap<K, T>()
    for (element in this) {
        result[keySelector(element)] = element
    }
    return result
}

/**
 * Returns a list of distinct elements preserving their original order.
 */
fun <T> Iterable<T>.orderedUnique(): List<T> {
    val seen = LinkedHashSet<T>()
    return filter { seen.add(it) }
}

/**
 * Suspending map — transforms each element using a suspend function sequentially.
 */
suspend fun <T, R> Iterable<T>.asyncMap(transform: suspend (T) -> R): List<R> {
    return map { transform(it) }
}

/**
 * Suspending filter — filters elements using a suspend predicate sequentially.
 */
suspend fun <T> Iterable<T>.asyncFilter(predicate: suspend (T) -> Boolean): List<T> {
    return filter { predicate(it) }
}
