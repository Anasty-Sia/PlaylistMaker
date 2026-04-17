package com.example.playlistmaker.search.domain.interactor.impl

import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository) : SearchHistoryInteractor {


    override suspend fun loadHistoryAndGet(): List<Track> {
        return repository.loadHistory()
    }

    override suspend fun addTrackToHistory(track: Track) {
        repository.addTrackToHistory(track)
    }

    override fun getSearchHistory(): Flow<List<Track>> {
        return repository.getSearchHistory()
    }

    override suspend fun clearSearchHistory() {
        repository.clearSearchHistory()
    }
}