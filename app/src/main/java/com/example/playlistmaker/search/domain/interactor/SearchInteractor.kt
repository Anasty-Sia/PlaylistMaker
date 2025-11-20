package com.example.playlistmaker.search.domain.interactor

import com.example.playlistmaker.search.domain.model.Track

interface SearchInteractor {
    suspend fun searchTracks(query: String): List<Track>
}
