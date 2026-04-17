package com.example.playlistmaker.search.domain.repository

import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    suspend fun loadHistory(): List<Track>
    suspend fun addTrackToHistory(track: Track)
    fun getSearchHistory(): Flow<List<Track>>
    suspend fun clearSearchHistory()
}