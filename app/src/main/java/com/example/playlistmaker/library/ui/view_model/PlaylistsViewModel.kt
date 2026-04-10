package com.example.playlistmaker.library.ui.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class PlaylistsViewModel( private val playlistsInteractor: PlaylistsInteractor
): ViewModel() {

    private val _playlistsState = MutableStateFlow<PlaylistsState>(PlaylistsState.Loading)
    val playlistsState: StateFlow<PlaylistsState> = _playlistsState.asStateFlow()

    init {
        loadPlaylists()
    }


    fun loadPlaylists() {

        _playlistsState.value = PlaylistsState.Loading
        viewModelScope.launch {
            try {
                playlistsInteractor.getAllPlaylists().collect { playlists ->
                    if (playlists.isEmpty()) {
                        _playlistsState.value = PlaylistsState.Empty
                    } else {
                        _playlistsState.value = PlaylistsState.Content(playlists)
                    }
                }
            } catch (e: Exception) {
                _playlistsState.value = PlaylistsState.Empty
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _playlistsState.value = PlaylistsState.Loading
            try {
                playlistsInteractor.getAllPlaylists().collect { playlists ->
                    if (playlists.isEmpty()) {
                        _playlistsState.value = PlaylistsState.Empty
                    } else {
                        _playlistsState.value = PlaylistsState.Content(playlists)
                    }
                }
            } catch (e: Exception) {
                _playlistsState.value = PlaylistsState.Empty
            }
        }
    }

    suspend fun createPlaylist(playlist: Playlist): Long {
        return playlistsInteractor.createPlaylist(playlist)


    }

    suspend fun updatePlaylist(playlist: Playlist) {
        playlistsInteractor.updatePlaylist(playlist)

    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistsInteractor.deletePlaylist(playlistId)

    }

    suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        playlistsInteractor.addTrackToPlaylist(playlistId, track)

    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int) {
        playlistsInteractor.deleteTrackFromPlaylist(playlistId, trackId)

    }

    suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return playlistsInteractor.getPlaylistById(playlistId)
    }
    sealed class PlaylistsState {
        object Empty : PlaylistsState()
        object Loading : PlaylistsState()
        data class Content(val playlists: List<Playlist>) : PlaylistsState()
    }
}