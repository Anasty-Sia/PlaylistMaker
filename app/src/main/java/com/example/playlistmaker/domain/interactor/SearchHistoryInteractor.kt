package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryInteractor(private val searchHistoryRepository: SearchHistoryRepository):SearchHistoryInteractorInterface  {
    override suspend fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrackToHistory(track)
    }

    override suspend fun getSearchHistory(): List<Track> {
        return searchHistoryRepository.getSearchHistory()
    }

    override suspend fun clearSearchHistory() {
        searchHistoryRepository.clearSearchHistory()
    }
}