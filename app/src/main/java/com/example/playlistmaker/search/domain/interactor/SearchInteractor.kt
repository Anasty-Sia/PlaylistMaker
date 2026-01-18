package com.example.playlistmaker.search.domain.interactor

import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
     fun searchTracks(query: String): Flow<List<Track>>
}
