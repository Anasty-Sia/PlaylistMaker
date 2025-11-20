package com.example.playlistmaker.player.domain.repository

import com.example.playlistmaker.player.domain.model.PlaybackState

interface PlayerRepository {
    fun preparePlayer(trackUrl: String)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun getPlaybackState(): PlaybackState
    fun getCurrentPosition(): Int
    fun getDuration(): Int
}