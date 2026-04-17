package com.example.playlistmaker.player.ui.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.player.service.PlayerService
import com.example.playlistmaker.player.service.PlayerServiceConnector
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _playerState  = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _isLoadingPlaylists = MutableLiveData<Boolean>(false)

    private val _showBottomSheet = MutableLiveData<Boolean>(false)
    val showBottomSheet: LiveData<Boolean> = _showBottomSheet

    private var serviceConnector: PlayerServiceConnector? = null
    private var isAppInForeground = true
    private var currentTrack: Track? = null
    private var pendingPrepare = false

    init {
        _playerState.value = PlayerState()
    }

    fun setTrack(track: Track) {
        currentTrack = track
        if (serviceConnector != null) {
            preparePlayer()
        } else {
            pendingPrepare = true
        }
    }

    fun bindService(connector: PlayerServiceConnector) {
        serviceConnector = connector
        serviceConnector?.setStateListener(object : PlayerService.PlayerStateListener {
            override fun onStateChanged(state: PlaybackState, currentPosition: Int) {
                _playerState.value = _playerState.value?.copy(
                    playbackState = state,
                    currentPosition = currentPosition
                )
                handleNotificationBasedOnState(state)
            }
        })

        if (pendingPrepare && currentTrack != null) {
            preparePlayer()
            pendingPrepare = false
        }
    }

    fun unbindService() {
        serviceConnector?.setStateListener(null)
        serviceConnector = null
    }

    fun setAppForegroundState(isForeground: Boolean) {
        isAppInForeground = isForeground
        updateNotificationVisibility()
    }



    private fun handleNotificationBasedOnState(state: PlaybackState) {
        when (state) {
            is PlaybackState.PLAYING -> {
                if (!isAppInForeground) {
                    serviceConnector?.showForegroundNotification()
                } else {
                    serviceConnector?.hideForegroundNotification()
                }
            }
            is PlaybackState.COMPLETED -> {
                serviceConnector?.hideForegroundNotification()
            }
            else -> {
                serviceConnector?.hideForegroundNotification()
            }
        }
    }

    private fun updateNotificationVisibility() {
        val currentState = _playerState.value?.playbackState
        if (currentState is PlaybackState.PLAYING) {
            if (!isAppInForeground) {
                serviceConnector?.showForegroundNotification()
            } else {
                serviceConnector?.hideForegroundNotification()
            }
        }
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


    fun preparePlayer() {
        viewModelScope.launch {
            try {
                val track = currentTrack ?: return@launch

                val isFavorite = withContext(Dispatchers.IO) {
                    favoriteTracksInteractor.isTrackFavorite(track.trackId)
                }
                _playerState.value = PlayerState(
                    playbackState = PlaybackState.PREPARED,
                    isFavorite = isFavorite,
                    currentPosition = 0
                )

                delay(100)
                serviceConnector?.preparePlayer()
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
            else -> {
             }
        }
    }

    private fun startPlayback() {
        serviceConnector?.startPlayer()
        _playerState.value = _playerState.value?.copy(playbackState = PlaybackState.PLAYING)
    }

    private fun pausePlayback() {
        serviceConnector?.pausePlayer()
        _playerState.value = _playerState.value?.copy(playbackState = PlaybackState.PAUSED)
    }

    fun stopPlayback() {
        serviceConnector?.stopPlayback()
        _playerState.value = _playerState.value?.copy(
            playbackState = PlaybackState.PREPARED,
            currentPosition = 0)
    }

    fun toggleFavorite() {
        viewModelScope.launch {
                val currentState = _playerState.value ?: return@launch
                val track =  currentTrack ?: return@launch

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

    fun clearAddToPlaylistStatus() {
        _playerState.value = _playerState.value?.copy(addToPlaylistStatus = null)
    }

    override fun onCleared() {
        super.onCleared()
        serviceConnector?.stopPlayback()
        serviceConnector = null
    }

    fun showPlaylistBottomSheet() {
        loadPlaylists()
        _showBottomSheet.value = true
    }

    fun hidePlaylistBottomSheet() {
        _showBottomSheet.value = false
    }

    fun releasePlayer() {
        serviceConnector?.stopPlayback()
        serviceConnector = null
    }

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }
}