package com.quranengine.core.utilities.features

/**
 * Retries [body] up to [times] attempts, returning the first successful result.
 * Throws the last exception if all attempts fail.
 */
inline fun <T> attempt(times: Int, body: () -> T): T {
    require(times > 0) { "times must be > 0" }
    var lastException: Throwable? = null
    repeat(times) {
        try {
            return body()
        } catch (e: Throwable) {
            lastException = e
        }
    }
    throw lastException!!
}

/**
 * Suspend variant: retries [body] up to [times] attempts.
 */
suspend inline fun <T> attemptSuspend(times: Int, body: () -> T): T {
    require(times > 0) { "times must be > 0" }
    var lastException: Throwable? = null
    repeat(times) {
        try {
            return body()
        } catch (e: Throwable) {
            lastException = e
        }
    }
    throw lastException!!
}
