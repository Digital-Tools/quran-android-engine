package com.quranengine.domain.readingservice

import com.quranengine.model.qurankit.Reading

/**
 * Production [ReadingRemoteResources]: every mushaf is downloaded from the
 * Mizan content CDN (Cloudflare R2 behind the `mizan-cdn` Worker; nothing is
 * bundled with the app). Only [hostedReadings] are on the CDN; the rest point
 * at a placeholder host, so [ReadingAvailability] shows them as "coming soon".
 * Mirrors quran-ios `MizanReadingRemoteResources`.
 *
 * Zip packaging contract (mizan-app `scripts/cdn/upload_mushaf.sh`): the zip
 * is `<localPath>/images_<width>.zip` with `width_<width>/` and `databases/`
 * at its top; [RemoteResource.extractDestination] unzips it into
 * `readings/<localPath>/images_<width>/`, where [Reading.imageResources] looks.
 */
class MizanReadingRemoteResources(
    private val mushafHost: String,
    private val hostedReadings: Set<Reading> = setOf(Reading.HAFS_1405),
) : ReadingRemoteResources {
    override fun resource(reading: Reading): RemoteResource {
        val host = if (reading in hostedReadings) mushafHost else PLACEHOLDER_HOST
        val images = reading.imageResources.imagesPath.substringBefore('/')
        return RemoteResource(
            url = "$host${reading.localPath}/$images.zip",
            reading = reading,
            version = 1,
        )
    }

    private companion object {
        /** `.example` is reserved (RFC 2606): never served, read as "not hosted". */
        const val PLACEHOLDER_HOST = "https://not-hosted-yet.example/"
    }
}
