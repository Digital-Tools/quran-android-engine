package com.quranengine.core.utilities.features

import kotlinx.coroutines.CompletableDeferred

/**
 * Coordinates one-time async initialization.
 * Call [initialize] when setup is complete; callers can [awaitInitialization] to suspend
 * until that happens.
 *
 * Port of Swift's AsyncInitializer using [CompletableDeferred].
 */
class AsyncInitializer {

    private val deferred = CompletableDeferred<Unit>()

    val initialized: Boolean
        get() = deferred.isCompleted

    /** Marks initialization as complete, resuming all waiters. */
    fun initialize() {
        deferred.complete(Unit)
    }

    /** Suspends until [initialize] is called. Returns immediately if already initialized. */
    suspend fun awaitInitialization() {
        deferred.await()
    }
}
