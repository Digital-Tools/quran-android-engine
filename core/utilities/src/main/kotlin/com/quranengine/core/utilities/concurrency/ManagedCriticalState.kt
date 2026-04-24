package com.quranengine.core.utilities.concurrency

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Thread-safe state container that protects mutable state behind a [ReentrantLock].
 * Port of Swift's ManagedCriticalState.
 */
class ManagedCriticalState<State>(initial: State) {
    private val lock = ReentrantLock()
    private var state: State = initial

    /**
     * Executes [critical] while holding the lock, providing mutable access to the state.
     * Returns whatever [critical] returns.
     */
    fun <R> withCriticalRegion(critical: (state: State) -> Pair<State, R>): R {
        return lock.withLock {
            val (newState, result) = critical(state)
            state = newState
            result
        }
    }

    /** Read the current state under the lock. */
    val value: State
        get() = lock.withLock { state }

    /** Mutate the state under the lock. */
    fun update(transform: (State) -> State) {
        lock.withLock { state = transform(state) }
    }
}
