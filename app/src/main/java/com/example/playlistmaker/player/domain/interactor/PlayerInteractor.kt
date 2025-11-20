package com.example.playlistmaker.player.domain.interactor

import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.search.domain.model.Track

interface PlayerInteractor {
    fun preparePlayer(track: Track)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun getPlaybackState(): PlaybackState
    fun getCurrentPosition(): Int
    fun getDuration(): Int
}
