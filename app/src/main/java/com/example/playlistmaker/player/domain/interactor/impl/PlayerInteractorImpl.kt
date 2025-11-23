package com.example.playlistmaker.player.domain.interactor.impl

import com.example.playlistmaker.player.domain.interactor.PlayerInteractor
import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.player.domain.repository.PlayerRepository
import com.example.playlistmaker.search.domain.model.Track

class PlayerInteractorImpl(
    private val playerRepository: PlayerRepository
) : PlayerInteractor {


    override fun preparePlayer(track: Track) {
        val previewUrl = track.previewUrl
        if (previewUrl != null) {
            playerRepository.preparePlayer(previewUrl)
        }
    }
    override fun startPlayer() {
        playerRepository.startPlayer()
    }

    override fun pausePlayer() {
        playerRepository.pausePlayer()
    }

    override fun releasePlayer() {
        playerRepository.releasePlayer()
    }

    override fun getPlaybackState(): PlaybackState {
        return playerRepository.getPlaybackState()
    }

    override fun getCurrentPosition(): Int {
        return playerRepository.getCurrentPosition()
    }

    override fun getDuration(): Int {
        return playerRepository.getDuration()
    }
}