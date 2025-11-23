package com.example.playlistmaker.search.domain.repository

import com.example.playlistmaker.search.domain.model.Track

interface SearchHistoryRepository {
    suspend fun addTrackToHistory(track: Track)
    suspend fun getSearchHistory(): List<Track>
    suspend fun clearSearchHistory()
}