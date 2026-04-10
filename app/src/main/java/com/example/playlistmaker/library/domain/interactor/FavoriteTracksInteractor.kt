package com.example.playlistmaker.library.domain.interactor

import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksInteractor {
    suspend fun addTrackToFavorites(track: Track)
    suspend fun removeTrackFromFavorites(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
    suspend fun isTrackFavorite(trackId: Int): Boolean

    suspend fun getAllFavoriteTracks(): List<Track>
    suspend fun toggleFavorite(track: Track): Boolean
    suspend fun isFavorite(trackId: Int): Boolean
}