package com.quranengine.core.caching

import com.quranengine.core.utilities.concurrency.ManagedCriticalState
import com.quranengine.core.utilities.concurrency.MulticastContinuation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Alias for an async operation that can be cached.
 */
typealias CacheableOperation<Input, Output> = suspend (Input) -> Output

/**
 * A service that caches the result of an async [operation] and deduplicates in-flight
 * requests for the same input using [MulticastContinuation].
 *
 * Port of Swift's OperationCacheableService.
 */
class OperationCacheableService<Input, Output>(
    cache: Cache<Input, Output>,
    private val operation: CacheableOperation<Input, Output>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private data class State<Input, Output>(
        val cache: Cache<Input, Output>,
        val inProgressOperations: MutableMap<Input, MulticastContinuation<Output>> = mutableMapOf()
    )

    private val state = ManagedCriticalState(State(cache))

    fun invalidate() {
        state.withCriticalRegion { current ->
            current.inProgressOperations.clear()
            current.cache.removeAllObjects()
            current to Unit
        }
    }

    suspend fun get(input: Input): Output {
        getCached(input)?.let { return it }

        val deferred = CompletableDeferred<Output>()

        state.withCriticalRegion { current ->
            val existing = current.inProgressOperations[input]
            if (existing != null) {
                existing.addDeferred(deferred)
            } else {
                val continuation = MulticastContinuation<Output>()
                continuation.addDeferred(deferred)
                current.inProgressOperations[input] = continuation
                startOperation(input, continuation)
            }
            current to Unit
        }

        return deferred.await()
    }

    fun getCached(input: Input): Output? {
        return state.withCriticalRegion { current ->
            current to current.cache.get(input)
        }
    }

    private fun startOperation(input: Input, continuation: MulticastContinuation<Output>) {
        scope.launch {
            val result = runCatching { operation(input) }

            state.withCriticalRegion { current ->
                if (result.isSuccess) {
                    current.cache.set(input, result.getOrThrow())
                }
                continuation.resumeWith(result)
                current.inProgressOperations.remove(input)
                current to Unit
            }
        }
    }
}
