package com.quranengine.core.utilities.features

/**
 * Compares two objects using a chain of predicates.
 * The first predicate that distinguishes the two objects determines the order.
 *
 * Port of Swift's MultiPredicateComparer.
 */
class MultiPredicateComparer<T>(
    private val predicates: List<(T, T) -> Int>,
) : Comparator<T> {

    constructor(vararg predicates: (T, T) -> Int) : this(predicates.toList())

    override fun compare(lhs: T, rhs: T): Int {
        for (predicate in predicates) {
            val result = predicate(lhs, rhs)
            if (result != 0) return result
        }
        return 0
    }

    companion object {
        /**
         * Creates a comparer from selectors that extract [Comparable] keys.
         * Each selector defines an ascending-order comparison.
         */
        fun <T> bySelectors(vararg selectors: (T) -> Comparable<*>): MultiPredicateComparer<T> {
            val predicates = selectors.map { selector ->
                { lhs: T, rhs: T ->
                    @Suppress("UNCHECKED_CAST")
                    val left = selector(lhs) as Comparable<Any>
                    val right = selector(rhs) as Comparable<Any>
                    left.compareTo(right)
                }
            }
            return MultiPredicateComparer(predicates)
        }
    }
}
