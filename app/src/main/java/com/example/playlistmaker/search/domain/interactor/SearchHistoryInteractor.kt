package com.example.playlistmaker.search.domain.interactor

import com.example.playlistmaker.search.domain.model.Track

interface SearchHistoryInteractor {
    suspend fun addTrackToHistory(track: Track)
    suspend fun getSearchHistory(): List<Track>
    suspend fun clearSearchHistory()
}