package com.example.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.interactor.PlayerInteractor
import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(

    private val playerInteractor: PlayerInteractor) : ViewModel() {

    private val _playbackState = MutableLiveData<PlaybackState>()
    val playbackState: LiveData<PlaybackState> = _playbackState

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> = _currentPosition

    private var progressJob: Job? = null
    private lateinit var currentTrack: Track

    init {
        _isFavorite.value = false
    }

    fun preparePlayer(track: Track) {
        currentTrack = track
        playerInteractor.preparePlayer(track)
        _playbackState.value = PlaybackState.PREPARED
    }

    fun playPause() {
        when (val currentState = _playbackState.value) {
            is PlaybackState.PLAYING -> pausePlayback()
            is PlaybackState.PREPARED, is PlaybackState.PAUSED, is PlaybackState.COMPLETED -> startPlayback()
            else -> {}
        }
    }

    private fun startPlayback() {
        playerInteractor.startPlayer()
        startProgressUpdates()
        _playbackState.value = PlaybackState.PLAYING
    }

    private fun pausePlayback() {
        playerInteractor.pausePlayer()
        progressJob?.cancel()
        _playbackState.value = PlaybackState.PAUSED
    }

    fun stopPlayback() {
        playerInteractor.pausePlayer()
        progressJob?.cancel()
        _playbackState.value = PlaybackState.PREPARED
    }

    fun toggleFavorite() {
        _isFavorite.value = !(_isFavorite.value ?: false)

    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                val position = playerInteractor.getCurrentPosition()
                _currentPosition.value = position

                when (playerInteractor.getPlaybackState()) {
                    is PlaybackState.COMPLETED -> {
                        _playbackState.value = PlaybackState.COMPLETED
                        progressJob?.cancel()
                    }
                    is PlaybackState.ERROR -> {
                        _playbackState.value = playerInteractor.getPlaybackState()
                        progressJob?.cancel()
                    }
                    else -> {

                    }
                }

                delay(PROGRESS_UPDATE_DELAY)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        playerInteractor.releasePlayer()
    }

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }
}