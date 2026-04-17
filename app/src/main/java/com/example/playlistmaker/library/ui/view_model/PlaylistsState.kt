package com.example.playlistmaker.library.ui.view_model

import com.example.playlistmaker.library.domain.model.Playlist

sealed class PlaylistsState {
    object Loading : PlaylistsState()
    object Empty : PlaylistsState()
    data class Content(val playlists: List<Playlist>) : PlaylistsState()
}