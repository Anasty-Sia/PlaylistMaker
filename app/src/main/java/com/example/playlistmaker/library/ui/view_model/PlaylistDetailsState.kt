package com.example.playlistmaker.library.ui.view_model

import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track

data class PlaylistDetailsState(
    val isLoading: Boolean = false,
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val totalDuration: String = "0:00"
)