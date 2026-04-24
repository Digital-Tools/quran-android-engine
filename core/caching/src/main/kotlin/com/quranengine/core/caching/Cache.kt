package com.quranengine.core.caching

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A thread-safe LRU cache with configurable size limits and low-memory awareness.
 *
 * Port of Swift's Cache<KeyType, ObjectType> which wraps NSCache.
 * Uses a [LinkedHashMap] with access-order for LRU eviction.
 *
 * @param lowMemoryCallback Optional callback registration for low-memory events.
 *   On Android, callers can wire this to [android.content.ComponentCallbacks2.onTrimMemory]
 *   without this module depending on the Android framework directly.
 */
class Cache<K, V>(
    lowMemoryCallback: (onLowMemory: () -> Unit) -> Unit = {}
) {

    private val lock = ReentrantLock()
    private val map = LinkedHashMap<K, V>(16, 0.75f, true)

    var name: String = ""

    @Volatile var countLimit: Int = 0

    init {
        lowMemoryCallback { removeAllObjects() }
    }

    fun get(key: K): V? = lock.withLock {
        map[key]
    }

    fun set(key: K, value: V) = lock.withLock {
        map[key] = value
        evictIfNeeded()
    }

    fun remove(key: K) = lock.withLock {
        map.remove(key)
    }

    fun removeAllObjects() = lock.withLock {
        map.clear()
    }

    private fun evictIfNeeded() {
        if (countLimit <= 0) return
        val iterator = map.iterator()
        while (map.size > countLimit && iterator.hasNext()) {
            iterator.next()
            iterator.remove()
        }
    }
}
