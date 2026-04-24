package com.quranengine.model.qurankit

/**
 * Returns the last element for which [predicate] is true, using binary search.
 *
 * Assumes the list is partitioned: [predicate] is true for some prefix of elements
 * and false for the rest. Returns the element just before the first false.
 */
fun <T> List<T>.binarySearchFirst(predicate: (T) -> Boolean): T {
    val index = binarySearchPartitionIndex(predicate)
    return this[index - 1]
}

/**
 * Finds the partition index N such that [predicate] is true for all elements
 * with index < N, and false for all elements with index >= N.
 *
 * Behavior is undefined if no such N exists.
 */
fun <T> List<T>.binarySearchPartitionIndex(predicate: (T) -> Boolean): Int {
    var low = 0
    var high = size
    while (low != high) {
        val mid = low + (high - low) / 2
        if (predicate(this[mid])) {
            low = mid + 1
        } else {
            high = mid
        }
    }
    return low
}
