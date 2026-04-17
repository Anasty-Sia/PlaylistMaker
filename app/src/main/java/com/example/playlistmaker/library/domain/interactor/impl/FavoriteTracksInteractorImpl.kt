package com.example.playlistmaker.library.domain.interactor.impl

import com.example.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.library.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository
) : FavoriteTracksInteractor {

    override suspend fun addTrackToFavorites(track: Track) {
        repository.addTrackToFavorites(track)
    }

    override suspend fun removeTrackFromFavorites(track: Track) {
        repository.removeTrackFromFavorites(track)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return repository.getFavoriteTracks()
    }

    override suspend fun isTrackFavorite(trackId: Int): Boolean {
        return repository.isTrackFavorite(trackId)
    }

    override suspend fun getAllFavoriteTracks(): List<Track> {
        return getFavoriteTracks().firstOrNull() ?: emptyList()
    }

    override suspend fun toggleFavorite(track: Track): Boolean {
        val isFav = isTrackFavorite(track.trackId)
        if (isFav) {
            removeTrackFromFavorites(track)
        } else {
            addTrackToFavorites(track)
        }
        return !isFav
    }

    override suspend fun isFavorite(trackId: Int): Boolean {
        return isTrackFavorite(trackId)
    }


}