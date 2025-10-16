package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryRepository {
    suspend fun addTrackToHistory(track: Track)
    suspend fun getSearchHistory(): List<Track>
    suspend fun clearSearchHistory()
}