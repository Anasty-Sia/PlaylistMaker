package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaylistsViewModel( private val playlistsInteractor: PlaylistsInteractor
): ViewModel(){

        private val _playlistsState = MutableLiveData<PlaylistsState>()
        val playlistsState: LiveData<PlaylistsState> = _playlistsState

         private var loadPlaylistsJob: Job? = null

         private val _playlistsFlow = MutableStateFlow<List<Playlist>>(emptyList())
         val playlists: StateFlow<List<Playlist>> = _playlistsFlow

        init {
            loadPlaylists()
        }

        fun loadPlaylists() {
            loadPlaylistsJob?.cancel()

            loadPlaylistsJob = viewModelScope.launch {
                playlistsInteractor.getAllPlaylists()
                    .collect { playlists ->
                    _playlistsFlow.value = playlists
                    if (playlists.isEmpty()) {
                        _playlistsState.postValue(PlaylistsState.Empty)
                    } else {
                        _playlistsState.postValue(PlaylistsState.Content(playlists))
                    }
                }
            }
        }

        suspend fun createPlaylist(playlist: Playlist): Long {
            val playlistId = playlistsInteractor.createPlaylist(playlist)
            loadPlaylists()
            return playlistId
        }


        suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
            playlistsInteractor.addTrackToPlaylist(playlistId, track)
            loadPlaylists()
        }


        fun refresh() {
            loadPlaylists()
        }

        override fun onCleared() {
            super.onCleared()
            loadPlaylistsJob?.cancel()
        }


    }

    sealed class PlaylistsState {
        object Empty : PlaylistsState()
        data class Content(val playlists: List<Playlist>) : PlaylistsState()
    }