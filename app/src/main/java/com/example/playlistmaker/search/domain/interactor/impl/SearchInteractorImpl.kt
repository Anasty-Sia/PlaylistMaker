package com.example.playlistmaker.search.domain.interactor.impl

import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.search.domain.model.Track

class SearchInteractorImpl(
    private val trackRepository: TrackRepository
) : SearchInteractor {

    override suspend fun searchTracks(query: String): List<Track> {
        return trackRepository.searchTracks(query)
    }
}