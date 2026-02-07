package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.launch

class PlaylistsViewModel( private val playlistsInteractor: PlaylistsInteractor
): ViewModel(){

        private val _playlistsState = MutableLiveData<PlaylistsState>()
        val playlistsState: LiveData<PlaylistsState> = _playlistsState

        init {
            loadPlaylists()
        }

        fun loadPlaylists() {
            viewModelScope.launch {
                playlistsInteractor.getAllPlaylists().collect { playlists ->
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
            loadPlaylists() // Перезагружаем список после создания
            return playlistId
        }

        suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
            playlistsInteractor.addTrackToPlaylist(playlistId, track)
        }


        fun refresh() {
            loadPlaylists()
        }
    }

    sealed class PlaylistsState {
        object Empty : PlaylistsState()
        data class Content(val playlists: List<Playlist>) : PlaylistsState()
    }