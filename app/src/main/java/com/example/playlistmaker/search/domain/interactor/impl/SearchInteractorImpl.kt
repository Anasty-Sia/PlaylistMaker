package com.example.playlistmaker.search.domain.interactor.impl

import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class SearchInteractorImpl(
    private val trackRepository: TrackRepository) : SearchInteractor {

    override fun searchTracks(query: String): Flow<List<Track>> {
        return trackRepository.searchTracks(query)
    }
}