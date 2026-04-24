package com.quranengine.core.system

import java.time.Instant

/**
 * Abstraction over the system clock.
 * Port of Swift's `SystemTime` protocol.
 */
interface SystemTime {
    val now: Instant
}

/**
 * Default implementation using [Instant.now].
 */
class DefaultSystemTime : SystemTime {
    override val now: Instant get() = Instant.now()
}
