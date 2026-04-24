package com.quranengine.core.utilities.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coroutine-based timer that supports repeating, pause, and resume.
 * Port of Swift's DispatchSourceTimer-based Timer.
 *
 * @param scope The coroutine scope to launch the timer in.
 * @param intervalMs Interval in milliseconds between firings.
 * @param repeated Whether the timer fires repeatedly or just once.
 * @param startNow If true, fires immediately on creation before waiting.
 * @param handler The action to execute on each tick.
 */
class Timer(
    private val scope: CoroutineScope,
    private val intervalMs: Long,
    private val repeated: Boolean = false,
    startNow: Boolean = false,
    private val handler: () -> Unit,
) {
    private var job: Job? = null
    private val mutex = Mutex()

    @Volatile
    private var _isCancelled = false

    @Volatile
    private var _isPaused = false

    val isCancelled: Boolean get() = _isCancelled

    init {
        job = scope.launch {
            if (!startNow) {
                delay(intervalMs)
            }
            if (repeated) {
                while (isActive && !_isCancelled) {
                    mutex.withLock {
                        if (!_isPaused && !_isCancelled) {
                            handler()
                        }
                    }
                    delay(intervalMs)
                }
            } else {
                mutex.withLock {
                    if (!_isCancelled && !_isPaused) {
                        handler()
                    }
                }
            }
        }
    }

    fun cancel() {
        _isCancelled = true
        job?.cancel()
        job = null
    }

    fun pause() {
        _isPaused = true
    }

    fun resume() {
        _isPaused = false
    }
}
