package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track

interface SearchInteractorInterface {
    suspend fun searchTracks(query: String): List<Track>
}