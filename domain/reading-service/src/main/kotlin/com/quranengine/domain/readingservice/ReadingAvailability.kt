package com.quranengine.domain.readingservice

import com.quranengine.model.qurankit.Reading
import java.net.URI

/**
 * Which readings can actually be opened in this build.
 *
 * A reading is usable when it is bundled (no remote resource) or its remote
 * resource lives on a real host. The `.example` placeholder host (RFC 2606)
 * can never serve a download, so selecting such a reading used to leave the
 * reader pointed at files that never arrive. The mushaf picker shows those
 * readings as "coming soon" instead. Mirrors quran-ios `ReadingAvailability`.
 */
object ReadingAvailability {
    fun isAvailable(reading: Reading, remoteResources: ReadingRemoteResources?): Boolean {
        val resource = remoteResources?.resource(reading) ?: return true
        val host = runCatching { URI(resource.url).host }.getOrNull().orEmpty()
        return host.isNotEmpty() && !host.endsWith(".example")
    }

    fun firstAvailable(remoteResources: ReadingRemoteResources?): Reading =
        Reading.sortedReadings.firstOrNull { isAvailable(it, remoteResources) } ?: Reading.HAFS_1405
}
