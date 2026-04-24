package com.quranengine.core.utilities.concurrency

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Thread-safe wrapper around a mutable value.
 * Port of Swift's Protected property wrapper.
 */
class Protected<T>(initial: T, private val lock: ReentrantLock = ReentrantLock()) {

    private var data: T = initial

    var value: T
        get() = lock.withLock { data }
        set(newValue) = lock.withLock { data = newValue }

    /**
     * Executes [body] with mutable access to the protected value under the lock.
     * The value returned by [body] becomes the new protected value.
     */
    fun <U> sync(body: (T) -> Pair<T, U>): U {
        return lock.withLock {
            val (newData, result) = body(data)
            data = newData
            result
        }
    }

    /** Convenience overload that mutates in place and returns Unit. */
    fun mutate(body: (T) -> T) {
        lock.withLock { data = body(data) }
    }
}
