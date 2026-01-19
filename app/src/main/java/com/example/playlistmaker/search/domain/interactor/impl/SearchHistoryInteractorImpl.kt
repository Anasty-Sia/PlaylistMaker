package com.example.playlistmaker.search.domain.interactor.impl

import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class SearchHistoryInteractorImpl(
    private val searchHistoryRepository: SearchHistoryRepository) : SearchHistoryInteractor {

    override suspend fun loadHistory() {
        searchHistoryRepository.loadHistory()
    }

    override suspend fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrackToHistory(track)
    }

    override fun getSearchHistory(): Flow<List<Track>> {
        return searchHistoryRepository.getSearchHistory()
    }

    override suspend fun clearSearchHistory() {
        searchHistoryRepository.clearSearchHistory()
    }
}