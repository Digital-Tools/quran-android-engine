package com.quranengine.core.utilities.concurrency

import kotlinx.coroutines.CompletableDeferred

/**
 * A continuation that can be awaited by multiple coroutines.
 * Port of Swift's MulticastContinuation using [CompletableDeferred].
 */
class MulticastContinuation<T> {

    private val state = ManagedCriticalState(State<T>())

    val isPending: Boolean
        get() = !state.value.completed

    /**
     * Adds a deferred that will be completed when [resume] is called.
     * If already completed, the deferred is completed immediately.
     */
    fun addDeferred(deferred: CompletableDeferred<T>) {
        val completedResult = state.withCriticalRegion { current ->
            if (current.completed) {
                current to current.result
            } else {
                val updated = current.copy(deferreds = current.deferreds + deferred)
                updated to null
            }
        }
        completedResult?.let { result ->
            result.fold(
                onSuccess = { deferred.complete(it) },
                onFailure = { deferred.completeExceptionally(it) },
            )
        }
    }

    /**
     * Creates and returns a new [CompletableDeferred] that will be completed
     * when this continuation is resumed.
     */
    fun await(): CompletableDeferred<T> {
        val deferred = CompletableDeferred<T>()
        addDeferred(deferred)
        return deferred
    }

    fun resume(value: T) {
        resumeWith(Result.success(value))
    }

    fun resume(error: Throwable) {
        resumeWith(Result.failure(error))
    }

    fun resumeWith(result: Result<T>) {
        val deferreds = state.withCriticalRegion { current ->
            val updated = current.copy(completed = true, result = result)
            updated to current.deferreds
        }
        for (deferred in deferreds) {
            result.fold(
                onSuccess = { deferred.complete(it) },
                onFailure = { deferred.completeExceptionally(it) },
            )
        }
    }

    private data class State<T>(
        val deferreds: List<CompletableDeferred<T>> = emptyList(),
        val completed: Boolean = false,
        val result: Result<T>? = null,
    )
}
