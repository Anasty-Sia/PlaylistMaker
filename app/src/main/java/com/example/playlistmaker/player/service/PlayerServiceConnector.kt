package com.example.playlistmaker.player.service

import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.search.domain.model.Track

interface PlayerServiceConnector {
    fun setTrack(track: Track)
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