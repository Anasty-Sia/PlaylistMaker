package com.example.playlistmaker.player.data.repository.impl

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.player.domain.repository.PlayerRepository
import java.io.IOException



class PlayerRepositoryImpl : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var currentState: PlaybackState = PlaybackState.DEFAULT

    override fun preparePlayer(trackUrl: String) {
        releasePlayer()

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(trackUrl)
                setOnPreparedListener {
                    currentState = PlaybackState.PREPARED
                }
                setOnCompletionListener {
                    currentState = PlaybackState.COMPLETED
                }
                setOnErrorListener { _, what, extra ->
                    currentState = PlaybackState.ERROR("MediaPlayer error: $what, $extra")
                    false
                }
                prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
                currentState = PlaybackState.ERROR("IOException: ${e.message}")
            }
        }
    }

    override fun startPlayer() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                currentState = PlaybackState.PLAYING
            }
        }
    }

    override fun pausePlayer() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                currentState = PlaybackState.PAUSED
            }
        }
    }

    override fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentState = PlaybackState.DEFAULT
    }

    override fun getPlaybackState(): PlaybackState {
        return currentState
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
}