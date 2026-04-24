package com.quranengine.model.qurankit

interface Navigatable<T : Navigatable<T>> : Comparable<T> {
    val next: T?
    val previous: T?
}

/**
 * Builds a list of elements from `this` to [end] (inclusive),
 * following the [Navigatable.next] chain.
 */
fun <T : Navigatable<T>> T.arrayTo(end: T): List<T> {
    require(end >= this) { "End ${this::class.simpleName} is less than first one." }
    val values = mutableListOf(this)
    var pointer: T = this
    while (true) {
        val n = pointer.next ?: break
        if (n > end) break
        pointer = n
        values.add(n)
    }
    return values
}
