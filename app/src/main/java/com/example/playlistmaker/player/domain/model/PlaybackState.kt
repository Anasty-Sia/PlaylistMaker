package com.example.playlistmaker.player.domain.model

sealed class PlaybackState {
    object DEFAULT : PlaybackState()
    object PREPARED : PlaybackState()
    object PLAYING : PlaybackState()
    object PAUSED : PlaybackState()
    object COMPLETED : PlaybackState()
    data class ERROR(val message: String) : PlaybackState()
}