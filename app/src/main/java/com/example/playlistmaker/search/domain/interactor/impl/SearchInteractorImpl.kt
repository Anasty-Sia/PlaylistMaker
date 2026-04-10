package com.example.playlistmaker.search.domain.interactor.impl

import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.search.domain.model.Track

import kotlinx.coroutines.flow.first

class SearchInteractorImpl(
    private val repository: TrackRepository
) : SearchInteractor {


    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            repository.searchTracks(query).first()
        } catch (e: Exception) {
            emptyList()
        }
    }
}