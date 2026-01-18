package com.example.playlistmaker.search.domain.interactor

import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchHistoryInteractor {
    suspend fun addTrackToHistory(track: Track)
    fun getSearchHistory(): Flow<List<Track>>
    suspend fun clearSearchHistory()

}