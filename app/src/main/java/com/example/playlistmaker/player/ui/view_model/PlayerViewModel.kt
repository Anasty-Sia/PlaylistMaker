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

data class PlayerState(
    val playbackState: PlaybackState = PlaybackState.PREPARED,
    val isFavorite: Boolean = false,
    val currentPosition: Int = 0
)

class PlayerViewModel(

    private val playerInteractor: PlayerInteractor) : ViewModel() {

    private val _playerState  = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private var progressJob: Job? = null
    private lateinit var currentTrack: Track

    init {
        _playerState.value = PlayerState()
    }

    fun preparePlayer(track: Track) {
        currentTrack = track
        _playerState.value = PlayerState(playbackState = PlaybackState.PREPARED)
        playerInteractor.preparePlayer(track)
    }

    fun playPause() {
        when (_playerState.value?.playbackState) {
            is PlaybackState.PLAYING -> pausePlayback()
            is PlaybackState.PREPARED, is PlaybackState.PAUSED, is PlaybackState.COMPLETED -> startPlayback()
            else -> {}
        }
    }

    private fun startPlayback() {
        playerInteractor.startPlayer()
        startProgressUpdates()
        _playerState.value = _playerState.value?.copy(playbackState = PlaybackState.PLAYING)
    }

    private fun pausePlayback() {
        playerInteractor.pausePlayer()
        progressJob?.cancel()
        _playerState.value = _playerState.value?.copy(playbackState = PlaybackState.PAUSED)
    }

    fun stopPlayback() {
        playerInteractor.pausePlayer()
        progressJob?.cancel()
        _playerState.value = _playerState.value?.copy(
            playbackState = PlaybackState.PREPARED,
            currentPosition = 0)
    }

    fun toggleFavorite() {
        _playerState.value = _playerState.value?.copy(
            isFavorite = !(_playerState.value?.isFavorite ?: false)
        )
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                val position = playerInteractor.getCurrentPosition()
                _playerState.value = _playerState.value?.copy(currentPosition = position)

                when (playerInteractor.getPlaybackState()) {
                    is PlaybackState.COMPLETED -> {
                        _playerState.value = _playerState.value?.copy(playbackState = PlaybackState.COMPLETED)
                        progressJob?.cancel()
                    }
                    is PlaybackState.ERROR -> {
                        _playerState.value = _playerState.value?.copy(playbackState = playerInteractor.getPlaybackState())
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