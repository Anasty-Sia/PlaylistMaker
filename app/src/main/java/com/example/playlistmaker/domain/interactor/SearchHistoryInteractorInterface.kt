package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryInteractorInterface {
    suspend fun addTrackToHistory(track: Track)
    suspend fun getSearchHistory(): List<Track>
    suspend fun clearSearchHistory()
}