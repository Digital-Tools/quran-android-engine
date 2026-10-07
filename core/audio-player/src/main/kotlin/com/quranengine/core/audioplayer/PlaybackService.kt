package com.quranengine.core.audioplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import timber.log.Timber

/**
 * Foreground service that exists only while a recitation is playing.
 *
 * The player itself lives in the app process and drives frame changes with main-looper
 * timers. Without a foreground service Android demotes the process as soon as the screen
 * locks: the timers stop running (the CPU sleeps), the next surah never starts and the
 * audio dies. This service keeps the process at foreground priority and holds a partial
 * wake lock, and gives the user a notification with pause and stop controls.
 *
 * It has no player of its own; [QueuePlayer] starts and stops it and receives the
 * notification's buttons through [Controls].
 */
class PlaybackService : Service() {

    /** Wired up by [QueuePlayer] for the notification's buttons. Main thread only. */
    internal object Controls {
        var onTogglePause: (() -> Unit)? = null
        var onStop: (() -> Unit)? = null
    }

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        createChannel()
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
            ACTION_TOGGLE_PAUSE -> Controls.onTogglePause?.invoke()
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

    private fun refreshNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
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

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val toggleLabel = if (paused) "Resume" else "Pause"
        val toggleIcon = if (paused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause

        return builder
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title ?: "Quran recitation")
            .setContentText(artist)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .addAction(Notification.Action.Builder(toggleIcon, toggleLabel, serviceIntent(ACTION_TOGGLE_PAUSE, 1)).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "Stop", serviceIntent(ACTION_STOP, 2)).build())
            .setStyle(Notification.MediaStyle().setShowActionsInCompactView(0, 1))
            .build()
    }

    private fun serviceIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, PlaybackService::class.java).setAction(action)
        return PendingIntent.getService(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        private const val CHANNEL_ID = "quran_playback"
        private const val NOTIFICATION_ID = 7421
        private const val ACTION_TOGGLE_PAUSE = "com.quranengine.core.audioplayer.TOGGLE_PAUSE"
        private const val ACTION_STOP = "com.quranengine.core.audioplayer.STOP"
        private const val WAKE_LOCK_TIMEOUT_MS = 6L * 60 * 60 * 1000

        @Volatile private var instance: PlaybackService? = null
        @Volatile private var title: String? = null
        @Volatile private var artist: String? = null
        @Volatile private var paused: Boolean = false

        /** Start (or keep) the service for a recitation that is starting. */
        internal fun start(context: Context) {
            paused = false
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
            context.stopService(Intent(context, PlaybackService::class.java))
        }

        /** Show what is playing. Cheap to call often; only a change updates the notification. */
        internal fun updateInfo(newTitle: String?, newArtist: String?) {
            if (newTitle == title && newArtist == artist) return
            title = newTitle
            artist = newArtist
            instance?.refreshNotification()
        }

        /** Switch the notification between its pause and resume button. */
        internal fun updatePaused(isPaused: Boolean) {
            if (isPaused == paused) return
            paused = isPaused
            instance?.refreshNotification()
        }
    }
}
