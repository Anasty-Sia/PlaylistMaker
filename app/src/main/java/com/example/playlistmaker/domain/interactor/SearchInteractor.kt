package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchInteractor(private val trackRepository: TrackRepository): SearchInteractorInterface  {
    override suspend fun searchTracks(query: String): List<Track> {
        return if (query.isNotBlank()) {
            trackRepository.searchTracks(query)
        } else {
            emptyList()
        }
    }
}
