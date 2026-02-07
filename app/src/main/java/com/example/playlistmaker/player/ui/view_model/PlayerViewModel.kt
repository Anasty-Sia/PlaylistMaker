package com.example.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
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
    val currentPosition: Int = 0,
    val addToPlaylistStatus: AddToPlaylistStatus? = null

)
sealed class AddToPlaylistStatus {
    data class Success(val playlistName: String) : AddToPlaylistStatus()
    data class AlreadyInPlaylist(val playlistName: String) : AddToPlaylistStatus()
    data class Error(val message: String) : AddToPlaylistStatus()
}


class PlayerViewModel(

    private val playerInteractor: PlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _playerState  = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _isLoadingPlaylists = MutableLiveData<Boolean>(false)

    private var progressJob: Job? = null
    private lateinit var currentTrack: Track

    init {
        _playerState.value = PlayerState()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _isLoadingPlaylists.value = true
            try {
                playlistsInteractor.getAllPlaylists().collect { playlistsList ->
                    _playlists.value = playlistsList
                    if (_isLoadingPlaylists.value == true) {
                        _isLoadingPlaylists.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoadingPlaylists.value = false
            }
        }
    }

    suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        try {
            val playlist = _playlists.value?.find { it.playlistId == playlistId }
            playlist?.let {
                if (it.trackIds.contains(track.trackId)) {
                    _playerState.value = _playerState.value?.copy(
                        addToPlaylistStatus = AddToPlaylistStatus.AlreadyInPlaylist(it.name)
                    )
                    resetAddToPlaylistStatus()
                    return
                }
            }

            playlistsInteractor.addTrackToPlaylist(playlistId, track)

            loadPlaylists()

            _playerState.value = _playerState.value?.copy(
                addToPlaylistStatus = AddToPlaylistStatus.Success(playlist?.name ?: "")
            )

            resetAddToPlaylistStatus()

        } catch (e: IllegalStateException) {
            val playlist = _playlists.value?.find { it.playlistId == playlistId }
            _playerState.value = _playerState.value?.copy(
                addToPlaylistStatus = AddToPlaylistStatus.AlreadyInPlaylist(playlist?.name ?: "")
            )
            resetAddToPlaylistStatus()
        } catch (e: Exception) {
            _playerState.value = _playerState.value?.copy(
                addToPlaylistStatus = AddToPlaylistStatus.Error("Ошибка при добавлении трека")
            )
            resetAddToPlaylistStatus()
            e.printStackTrace()
        }
    }

    private fun resetAddToPlaylistStatus() {
        viewModelScope.launch {
            delay(1500)
            _playerState.value = _playerState.value?.copy(addToPlaylistStatus = null)
        }
    }


    fun preparePlayer(track: Track) {
        currentTrack = track
        viewModelScope.launch {
            try {
                val isFavorite = favoriteTracksInteractor.isTrackFavorite(track.trackId)
                _playerState.value = PlayerState(
                    playbackState = PlaybackState.PREPARED,
                    isFavorite = isFavorite,
                    currentPosition = 0
                )
                playerInteractor.preparePlayer(track)
            } catch (e: Exception) {
                _playerState.value = PlayerState(
                    playbackState = PlaybackState.ERROR("Ошибка загрузки состояния"),
                    isFavorite = false,
                )
            }
        }
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
        viewModelScope.launch {
                val currentState = _playerState.value ?: return@launch
                val track = currentTrack

                if (currentState.isFavorite) {
                    favoriteTracksInteractor.removeTrackFromFavorites(track)
                } else {
                    favoriteTracksInteractor.addTrackToFavorites(track)
                }
                _playerState.value = _playerState.value?.copy(
                    isFavorite = !currentState.isFavorite
                )
        }
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

    fun clearAddToPlaylistStatus() {
        _playerState.value = _playerState.value?.copy(addToPlaylistStatus = null)
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