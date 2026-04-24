package com.quranengine.core.utilities.extensions

import java.io.InterruptedIOException
import kotlinx.coroutines.CancellationException

/**
 * Returns `true` if this throwable represents a cancellation.
 * Checks for coroutine [CancellationException] and I/O interruption.
 */
val Throwable.isCancelled: Boolean
    get() = this is CancellationException || this is InterruptedIOException
