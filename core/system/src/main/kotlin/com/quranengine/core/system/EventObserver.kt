package com.quranengine.core.system

import kotlinx.coroutines.channels.Channel

/**
 * A simple event signalling mechanism.
 * Port of Swift's `EventObserver` protocol.
 */
interface EventObserver {
    /** Signals that an event has occurred. Fire-and-forget, never suspends. */
    fun signal()

    /** Suspends until the next event is signalled via [signal]. */
    suspend fun waitForNextEvent()
}

/** Invoke the observer as a function (mirrors Swift's `callAsFunction`). */
operator fun EventObserver.invoke() {
    signal()
}

/**
 * Default implementation backed by a conflated [Channel].
 * Only the most recent signal is retained; earlier undelivered signals are dropped.
 */
class DefaultEventObserver : EventObserver {

    private val channel = Channel<Unit>(Channel.CONFLATED)

    override fun signal() {
        channel.trySend(Unit)
    }

    override suspend fun waitForNextEvent() {
        channel.receive()
    }
}
