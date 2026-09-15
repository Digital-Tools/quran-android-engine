package com.quranengine.domain.readingservice

import com.quranengine.model.qurankit.Reading

/**
 * Production [ReadingRemoteResources]: Hafs 1405 stays bundled with the app
 * (no download needed), every other reading is a zip fetched from
 * [mushafHost].
 *
 * Zip packaging contract: whoever prepares a reading's zip must lay it out
 * so that, once unzipped into `readings/<reading.localPath>/`, the files end
 * up at the same relative paths [Reading.imageResources] already expects —
 * e.g. for Hafs 1405-style readings, `images_1920/databases/ayahinfo_1920.db`
 * and `images_1920/width_1920/page001.png`…`page604.png`. Get this wrong and
 * the reading will download successfully but still fail to render.
 */
class MizanReadingRemoteResources(
    private val mushafHost: String,
) : ReadingRemoteResources {
    override fun resource(reading: Reading): RemoteResource? {
        if (reading == Reading.HAFS_1405) return null
        return RemoteResource(
            // TODO(mizan-hosting): mushafHost is a placeholder — no real
            // mushaf content is hosted anywhere yet. Swap the "mushafHost"
            // value at its single @Provides site once real hosting exists;
            // nothing else needs to change.
            url = "$mushafHost${reading.localPath}.zip",
            reading = reading,
            version = 1,
        )
    }
}
