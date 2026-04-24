package com.quranengine.core.audioplayer

import android.net.Uri

/**
 * Metadata describing the currently playing item, used to populate the
 * media-session / lock-screen controls.
 *
 * Port of the iOS `PlayerItemInfo` (title, artist, artwork).
 * On Android we reference artwork via a [Uri] rather than a bitmap.
 *
 * @param title      Track / surah title.
 * @param artist     Reciter name.
 * @param artworkUri Optional [Uri] pointing to album-art (content:// or file://).
 */
data class PlayerItemInfo(
    val title: String,
    val artist: String,
    val artworkUri: Uri? = null,
)
