package com.example.playlistmaker.library.domain.repository

import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    suspend fun addTrackToFavorites(track: Track)
    suspend fun removeTrackFromFavorites(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
    suspend fun getFavoriteTrackIds(): List<Int>
    suspend fun isTrackFavorite(trackId: Int): Boolean
}