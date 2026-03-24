package com.example.playlistmaker.player.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.*
import java.io.IOException

class PlayerService : Service() {

    private val binder = PlayerBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null
    private var currentState: PlaybackState = PlaybackState.DEFAULT

    private var progressJob: Job? = null
    private var stateListener: PlayerStateListener? = null
    private var isForegroundActive = false

    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService

        fun setTrack(track: Track) {
            currentTrack = track
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        intent?.getParcelableExtra<Track>("track")?.let { track ->
            currentTrack = track
        }
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    override fun onDestroy() {
        stopPlayback()
        releasePlayer()
        progressJob?.cancel()
        hideForegroundNotification()
        super.onDestroy()
    }

    fun preparePlayer() {
        val track = currentTrack ?: return
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(track.previewUrl ?: return)
                setOnPreparedListener {
                    currentState = PlaybackState.PREPARED
                    notifyStateChanged()
                }
                setOnCompletionListener {
                    currentState = PlaybackState.COMPLETED
                    notifyStateChanged()
                    hideForegroundNotification()
                    stopProgressUpdates()
                }
                setOnErrorListener { _, what, extra ->
                    currentState = PlaybackState.ERROR("Ошибка MediaPlayer: $what, $extra")
                    notifyStateChanged()
                    false
                }
                prepareAsync()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            currentState = PlaybackState.ERROR("Ошибка: ${e.message}")
            notifyStateChanged()
        }
    }

    fun preparePlayer(track: Track) {
        currentTrack = track
        preparePlayer()
    }

    fun startPlayer() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying && (currentState is PlaybackState.PREPARED ||
                        currentState is PlaybackState.PAUSED)) {
                player.start()
                currentState = PlaybackState.PLAYING
                notifyStateChanged()
                startProgressUpdates()

                if (currentState is PlaybackState.PLAYING) {
                    showForegroundNotification()
                }
            }
        }
    }

    fun pausePlayer() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                currentState = PlaybackState.PAUSED
                notifyStateChanged()
                stopProgressUpdates()
            }
        }
    }

    fun stopPlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            }
            currentState = PlaybackState.PREPARED
            notifyStateChanged()
            stopProgressUpdates()
            hideForegroundNotification()
        }
    }

    fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentState = PlaybackState.DEFAULT
        stopProgressUpdates()
    }

    fun getPlaybackState(): PlaybackState = currentState

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun setStateListener(listener: PlayerStateListener?) {
        stateListener = listener
    }

    fun showForegroundNotification() {
        val track = currentTrack ?: return
        if (currentState !is PlaybackState.PLAYING) return

        val notification = createNotification(track)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isForegroundActive = true
    }

    fun hideForegroundNotification() {
        if (isForegroundActive) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForegroundActive = false
        }
    }

    private fun createNotification(track: Track): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.title))
            .setContentText("${track.artistName} - ${track.trackName}")
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления о воспроизведении музыки"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && (currentState is PlaybackState.PLAYING)) {
                delay(300)
                notifyStateChanged()
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    private fun notifyStateChanged() {
        stateListener?.onStateChanged(
            currentState,
            getCurrentPosition()
        )
    }

    interface PlayerStateListener {
        fun onStateChanged(state: PlaybackState, currentPosition: Int)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "player_channel"
        private const val CHANNEL_NAME = "Уведомления плеера"
    }
}