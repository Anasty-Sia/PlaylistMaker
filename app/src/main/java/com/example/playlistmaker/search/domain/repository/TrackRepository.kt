package com.example.playlistmaker.search.domain.repository

import com.example.playlistmaker.search.domain.model.Track

interface TrackRepository {
    suspend fun searchTracks(query: String): List<Track>
}