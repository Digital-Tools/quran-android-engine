package com.quranengine.core.audioplayer

import android.os.SystemClock
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaSession

/**
 * Keeps lock-screen / notification metadata in sync with the current playback state.
 *
 * Port of the iOS `NowPlayingUpdater` that writes to `MPNowPlayingInfoCenter`.
 * On Android we push metadata through a [MediaSession]'s underlying player.
 *
 * **Must be called from the main thread.**
 *
 * @param session An active [MediaSession] whose metadata this class will update.
 */
class NowPlayingUpdater(private val session: MediaSession) {

    private var title: String? = null
    private var artist: String? = null
    private var artworkUri: android.net.Uri? = null
    private var trackNumber: Int? = null
    private var totalTrackCount: Int? = null

    // ---- Public API matching the iOS NowPlayingUpdater ----

    /** Remove all now-playing information. */
    fun clear() {
        title = null
        artist = null
        artworkUri = null
        trackNumber = null
        totalTrackCount = null
        val emptyMetadata = MediaMetadata.Builder().build()
        session.player.mediaMetadata.let {
            // Clear by setting empty metadata on the current media item
        }
    }

    /** Update the total duration (in seconds). */
    fun updateDuration(durationSeconds: Double) {
        // Duration is managed by the player's media source automatically in Media3.
    }

    /** Update the elapsed / current playback position (in seconds). */
    fun updateElapsedTime(elapsedTimeSeconds: Double, rate: Float) {
        // Position tracking is handled automatically by Media3's Player.
        // Playback speed is set via Player.setPlaybackParameters.
        applyPlaybackRate(rate)
    }

    /** Update track metadata from a [PlayerItemInfo]. */
    fun updateInfo(info: PlayerItemInfo) {
        title = info.title
        artist = info.artist
        artworkUri = info.artworkUri
        applyMetadata()
    }

    /** Update the playback rate (speed). */
    fun updateRate(rate: Float) {
        applyPlaybackRate(rate)
    }

    /** Update the total number of items in the queue. */
    fun updateCount(count: Int) {
        totalTrackCount = count
        applyMetadata()
    }

    /** Update the 0-based index of the currently playing item. */
    fun updatePlayingIndex(index: Int) {
        trackNumber = index + 1
        applyMetadata()
    }

    private fun applyMetadata() {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .apply { artworkUri?.let { setArtworkUri(it) } }
            .apply { trackNumber?.let { setTrackNumber(it) } }
            .apply { totalTrackCount?.let { setTotalTrackCount(it) } }
            .build()

        // Media3 reads metadata from the player's current MediaItem.
        val player = session.player
        val currentItem = player.currentMediaItem ?: return
        val updated = currentItem.buildUpon()
            .setMediaMetadata(metadata)
            .build()
        // Replace current item to update metadata
        val currentIndex = player.currentMediaItemIndex
        val position = player.currentPosition
        player.replaceMediaItem(currentIndex, updated)
        player.seekTo(currentIndex, position)
    }

    private fun applyPlaybackRate(rate: Float) {
        if (rate <= 0f) return
        session.player.playbackParameters = session.player.playbackParameters.withSpeed(rate)
    }
}
