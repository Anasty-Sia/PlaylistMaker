package com.example.playlistmaker.player.service

import com.example.playlistmaker.player.domain.model.PlaybackState

interface PlayerServiceConnector {
    fun preparePlayer()
    fun startPlayer()
    fun pausePlayer()
    fun stopPlayback()
    fun getPlaybackState(): PlaybackState
    fun getCurrentPosition(): Int
    fun getDuration(): Int
    fun showForegroundNotification()
    fun hideForegroundNotification()
    fun setStateListener(listener: PlayerService.PlayerStateListener?)
}