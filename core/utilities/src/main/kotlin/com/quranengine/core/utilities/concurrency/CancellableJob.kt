package com.quranengine.core.utilities.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * A cancellable wrapper around a coroutine [Job].
 * Automatically cancels the job when the task is no longer referenced
 * (call [cancel] explicitly—Kotlin has no `deinit`).
 *
 * Port of Swift's CancellableTask.
 */
class CancellableJob(private val job: Job) {

    val isActive: Boolean get() = job.isActive

    fun cancel() {
        job.cancel()
    }

    fun invokeOnCompletion(handler: (Throwable?) -> Unit) {
        job.invokeOnCompletion(handler)
    }

    override fun equals(other: Any?): Boolean =
        other is CancellableJob && job === other.job

    override fun hashCode(): Int = System.identityHashCode(job)
}

/**
 * Launches a coroutine in this scope, wraps it in a [CancellableJob],
 * and adds it to the mutable set for lifecycle tracking.
 */
fun MutableSet<CancellableJob>.launch(
    scope: CoroutineScope,
    block: suspend CoroutineScope.() -> Unit,
): CancellableJob {
    val job = scope.launch(block = block)
    val cancellable = CancellableJob(job)
    add(cancellable)
    job.invokeOnCompletion { remove(cancellable) }
    return cancellable
}

/** Cancels all jobs in this set and clears it. */
fun MutableSet<CancellableJob>.cancelAll() {
    forEach { it.cancel() }
    clear()
}

/** Wraps this [Job] into a [CancellableJob]. */
fun Job.asCancellableJob(): CancellableJob = CancellableJob(this)
