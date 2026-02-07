package com.example.playlistmaker.library.data.repository.impl

import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.data.db.FavoriteTrackEntity
import com.example.playlistmaker.library.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val database: AppDatabase
) : FavoriteTracksRepository {

    override suspend fun addTrackToFavorites(track: Track) {

        val existingTrack = database.favoriteTracksDao().getTrackById(track.trackId)
        if (existingTrack != null) return
            val entity = FavoriteTrackEntity(
                trackId = track.trackId,
                trackName = track.trackName,
                artistName = track.artistName,
                trackTimeMillis = track.trackTimeMillis,
                artworkUrl100 = track.artworkUrl100,
                collectionName = track.collectionName,
                releaseDate = track.releaseDate,
                primaryGenreName = track.primaryGenreName,
                country = track.country,
                previewUrl = track.previewUrl
            )
            database.favoriteTracksDao().insertTrack(entity)
    }

    override suspend fun removeTrackFromFavorites(track: Track) {
        database.favoriteTracksDao().deleteTrackById(track.trackId)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {

        return database.favoriteTracksDao().getAllTracks().map { entities ->
            entities.map { entity ->
                Track(
                    trackId = entity.trackId,
                    trackName = entity.trackName,
                    artistName = entity.artistName,
                    trackTimeMillis = entity.trackTimeMillis,
                    artworkUrl100 = entity.artworkUrl100,
                    collectionName = entity.collectionName,
                    releaseDate = entity.releaseDate,
                    primaryGenreName = entity.primaryGenreName,
                    country = entity.country,
                    previewUrl = entity.previewUrl,
                    isFavorite = true
                )
            }
        }
    }


    override suspend fun getFavoriteTrackIds(): List<Int> {
        return database.favoriteTracksDao().getAllFavoriteTrackIds()
    }

    override suspend fun isTrackFavorite(trackId: Int): Boolean {
        return database.favoriteTracksDao().isTrackFavorite(trackId)
    }
}