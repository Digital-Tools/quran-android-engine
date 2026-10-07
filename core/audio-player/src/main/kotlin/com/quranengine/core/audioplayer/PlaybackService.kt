package com.quranengine.core.audioplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import timber.log.Timber

/**
 * Foreground service that exists only while a recitation is playing.
 *
 * The player itself lives in the app process and drives frame changes with main-looper
 * timers. Without a foreground service Android demotes the process as soon as the screen
 * locks: the timers stop running (the CPU sleeps), the next surah never starts and the
 * audio dies. This service keeps the process at foreground priority and holds a partial
 * wake lock.
 *
 * It also owns the [MediaSession] behind the notification. With the session attached,
 * Android shows the recitation as a media player: large on the lock screen and at the
 * top of the notification shade, with previous / play-pause / next verse and stop, and
 * headset buttons reach it too. Without one it was an ordinary low-priority
 * notification under the others.
 *
 * It has no player of its own; [QueuePlayer] starts and stops it and receives the
 * controls through [Controls].
 *
 * The host app can set its own status-bar icon and artwork with `<meta-data>` in its
 * manifest: [META_NOTIFICATION_ICON] (a monochrome drawable) and [META_ARTWORK].
 */
class PlaybackService : Service() {

    /** Wired up by [QueuePlayer]: what the media controls do. Main thread only. */
    internal object Controls {
        var onPlay: (() -> Unit)? = null
        var onPause: (() -> Unit)? = null
        var onNext: (() -> Unit)? = null
        var onPrevious: (() -> Unit)? = null
        var onStop: (() -> Unit)? = null

        fun clear() {
            onPlay = null
            onPause = null
            onNext = null
            onPrevious = null
            onStop = null
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var session: MediaSession? = null
    private var artwork: Bitmap? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        createChannel()
        artwork = loadArtwork()
        session = createSession()
        applySession()
        // Promote right away: startForegroundService() requires it, even if a stop
        // follows before onStartCommand runs.
        startInForeground()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "quranengine:playback").apply {
            setReferenceCounted(false)
            // A safety net: a recitation never legitimately holds the CPU this long.
            acquire(WAKE_LOCK_TIMEOUT_MS)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE_PAUSE -> if (paused) Controls.onPlay?.invoke() else Controls.onPause?.invoke()
            ACTION_NEXT -> Controls.onNext?.invoke()
            ACTION_PREVIOUS -> Controls.onPrevious?.invoke()
            ACTION_STOP -> {
                Controls.onStop?.invoke()
                stopSelfSafely()
                return START_NOT_STICKY
            }
        }
        // Must be called within seconds of startForegroundService().
        startInForeground()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        instance = null
        session?.run {
            isActive = false
            release()
        }
        session = null
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        super.onDestroy()
    }

    private fun startInForeground() {
        val notification = buildNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            // The app was not allowed to go foreground (for example started from the
            // background). Playback still works while the app is visible.
            Timber.w(e, "Could not start the playback foreground service")
            stopSelfSafely()
        }
    }

    private fun refresh(notification: Boolean) {
        applySession()
        if (notification) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun stopSelfSafely() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Quran recitation", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Shown while a recitation is playing"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    // ---- Media session ----

    private fun createSession(): MediaSession = MediaSession(this, "QuranRecitation").apply {
        setCallback(object : MediaSession.Callback() {
            override fun onPlay() {
                Controls.onPlay?.invoke()
            }

            override fun onPause() {
                Controls.onPause?.invoke()
            }

            override fun onSkipToNext() {
                Controls.onNext?.invoke()
            }

            override fun onSkipToPrevious() {
                Controls.onPrevious?.invoke()
            }

            override fun onStop() {
                Controls.onStop?.invoke()
            }

            override fun onCustomAction(action: String, extras: Bundle?) {
                if (action == CUSTOM_ACTION_STOP) Controls.onStop?.invoke()
            }
        })
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        }
        contentIntent()?.let { setSessionActivity(it) }
        isActive = true
    }

    /** What the lock screen and the shade show: title, reciter, artwork, state and position. */
    private fun applySession() {
        val session = session ?: return
        val state = if (paused) PlaybackState.STATE_PAUSED else PlaybackState.STATE_PLAYING
        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(SESSION_ACTIONS)
                .setState(state, positionMs, if (paused) 0f else speed, positionAtMs)
                // Android 13+ draws its media controls from the session, not from the
                // notification's actions: stop has to be a custom action to appear there.
                .addCustomAction(
                    PlaybackState.CustomAction.Builder(CUSTOM_ACTION_STOP, "Stop", R.drawable.quran_playback_stop).build(),
                )
                .build(),
        )
        session.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title ?: DEFAULT_TITLE)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist.orEmpty())
                .apply { if (durationMs > 0) putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs) }
                .apply { artwork?.let { putBitmap(MediaMetadata.METADATA_KEY_ART, it) } }
                .build(),
        )
    }

    // ---- Notification ----

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        val toggle = if (paused) {
            action(R.drawable.quran_playback_play, "Play", ACTION_TOGGLE_PAUSE, 1)
        } else {
            action(R.drawable.quran_playback_pause, "Pause", ACTION_TOGGLE_PAUSE, 1)
        }

        // Android 12 and earlier show these actions; Android 13+ builds the controls
        // from the session (see applySession), in the same order.
        return builder
            .setSmallIcon(smallIcon())
            .setContentTitle(title ?: DEFAULT_TITLE)
            .setContentText(artist)
            .apply { artwork?.let { setLargeIcon(it) } }
            .setContentIntent(contentIntent())
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .addAction(action(R.drawable.quran_playback_previous, "Previous verse", ACTION_PREVIOUS, 2))
            .addAction(toggle)
            .addAction(action(R.drawable.quran_playback_next, "Next verse", ACTION_NEXT, 3))
            .addAction(action(R.drawable.quran_playback_stop, "Stop", ACTION_STOP, 4))
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(session?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2),
            )
            .build()
    }

    private fun action(icon: Int, label: String, action: String, requestCode: Int): Notification.Action {
        val intent = Intent(this, PlaybackService::class.java).setAction(action)
        val pending = PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return Notification.Action.Builder(Icon.createWithResource(this, icon), label, pending).build()
    }

    private fun contentIntent(): PendingIntent? =
        packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

    // ---- Host branding ----

    private fun metaDataResource(key: String): Int = try {
        @Suppress("DEPRECATION")
        packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData?.getInt(key, 0) ?: 0
    } catch (e: Exception) {
        0
    }

    private fun smallIcon(): Int =
        metaDataResource(META_NOTIFICATION_ICON).takeIf { it != 0 } ?: R.drawable.quran_playback_notification

    private fun loadArtwork(): Bitmap? {
        val resource = metaDataResource(META_ARTWORK).takeIf { it != 0 } ?: return null
        return try {
            val drawable = getDrawable(resource) ?: return null
            Bitmap.createBitmap(ARTWORK_SIZE_PX, ARTWORK_SIZE_PX, Bitmap.Config.ARGB_8888).also { bitmap ->
                drawable.setBounds(0, 0, ARTWORK_SIZE_PX, ARTWORK_SIZE_PX)
                drawable.draw(Canvas(bitmap))
            }
        } catch (e: Exception) {
            Timber.w(e, "Could not load the playback artwork")
            null
        }
    }

    companion object {
        /** Manifest `<meta-data>` key: the host app's status-bar icon (monochrome drawable). */
        const val META_NOTIFICATION_ICON = "com.quranengine.audioplayer.NOTIFICATION_ICON"

        /** Manifest `<meta-data>` key: artwork for the lock screen and the media player. */
        const val META_ARTWORK = "com.quranengine.audioplayer.ARTWORK"

        private const val CHANNEL_ID = "quran_playback"
        private const val NOTIFICATION_ID = 7421
        private const val ACTION_TOGGLE_PAUSE = "com.quranengine.core.audioplayer.TOGGLE_PAUSE"
        private const val ACTION_NEXT = "com.quranengine.core.audioplayer.NEXT"
        private const val ACTION_PREVIOUS = "com.quranengine.core.audioplayer.PREVIOUS"
        private const val ACTION_STOP = "com.quranengine.core.audioplayer.STOP"
        private const val CUSTOM_ACTION_STOP = "com.quranengine.core.audioplayer.CUSTOM_STOP"
        private const val DEFAULT_TITLE = "Quran recitation"
        private const val WAKE_LOCK_TIMEOUT_MS = 6L * 60 * 60 * 1000
        private const val ARTWORK_SIZE_PX = 512

        private const val SESSION_ACTIONS =
            PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_STOP

        @Volatile private var instance: PlaybackService? = null
        @Volatile private var title: String? = null
        @Volatile private var artist: String? = null
        @Volatile private var paused: Boolean = false
        @Volatile private var positionMs: Long = PlaybackState.PLAYBACK_POSITION_UNKNOWN
        @Volatile private var positionAtMs: Long = 0
        @Volatile private var durationMs: Long = -1
        @Volatile private var speed: Float = 1f

        /** Start (or keep) the service for a recitation that is starting. */
        internal fun start(context: Context) {
            paused = false
            positionMs = PlaybackState.PLAYBACK_POSITION_UNKNOWN
            durationMs = -1
            val intent = Intent(context, PlaybackService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Timber.w(e, "Could not start the playback service")
            }
        }

        /** Stop the service and clear its notification. */
        internal fun stop(context: Context) {
            title = null
            artist = null
            paused = false
            positionMs = PlaybackState.PLAYBACK_POSITION_UNKNOWN
            durationMs = -1
            context.stopService(Intent(context, PlaybackService::class.java))
        }

        /** Show what is playing. Cheap to call often; only a change updates the notification. */
        internal fun updateInfo(newTitle: String?, newArtist: String?) {
            if (newTitle == title && newArtist == artist) return
            title = newTitle
            artist = newArtist
            instance?.refresh(notification = true)
        }

        /**
         * Playing or paused, where in the file ([newPositionMs], negative when unknown) and
         * how long it is, so the lock screen's progress bar follows the recitation.
         */
        internal fun updatePlayback(isPaused: Boolean, newPositionMs: Long, newDurationMs: Long, newSpeed: Float) {
            val toggled = isPaused != paused
            paused = isPaused
            positionMs = if (newPositionMs >= 0) newPositionMs else PlaybackState.PLAYBACK_POSITION_UNKNOWN
            positionAtMs = SystemClock.elapsedRealtime()
            durationMs = newDurationMs
            if (newSpeed > 0f) speed = newSpeed
            // Only play / pause changes the notification itself; the rest is session state.
            instance?.refresh(notification = toggled)
        }
    }
}
