package com.example.playlistmaker.library.data.repository.impl

import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.data.db.PlaylistEntity
import com.example.playlistmaker.library.data.db.PlaylistTrackEntity
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.domain.repository.PlaylistsRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistsRepositoryImpl(
    private val database: AppDatabase
) : PlaylistsRepository {

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {

        val playlistEntity = database.playlistsDao().getPlaylistById(playlistId)
            ?: throw IllegalArgumentException("Playlist not found")

        val currentTrackIds = playlistEntity.getTrackIds()
        if (currentTrackIds.contains(track.trackId)) {
            throw IllegalStateException("Track already in playlist")
        }

        val playlistTrackEntity = PlaylistTrackEntity(
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

        val trackExists = database.playlistTracksDao().trackExists(track.trackId)
        if (!trackExists) {
            database.playlistTracksDao().insertTrack(playlistTrackEntity)
        }

        val updatedTrackIds = currentTrackIds + track.trackId
        val updatedEntity = playlistEntity.copy(
            trackIdsJson = PlaylistEntity.createTrackIdsJson(updatedTrackIds),
            trackCount = updatedTrackIds.size
        )

        database.playlistsDao().updatePlaylist(updatedEntity)


    }

   override suspend fun getPlaylistTracks(playlistId: Long): List<Track> {
        val playlistEntity = database.playlistsDao().getPlaylistById(playlistId)
            ?: return emptyList()

        val trackIds = playlistEntity.getTrackIds()
        val tracks = mutableListOf<Track>()

        for (trackId in trackIds) {
            val trackEntity = database.playlistTracksDao().getTrackById(trackId)
            trackEntity?.let {
                val isFavorite = database.favoriteTracksDao().isTrackFavorite(trackId)
                tracks.add(Track(
                    trackId = it.trackId,
                    trackName = it.trackName,
                    artistName = it.artistName,
                    trackTimeMillis = it.trackTimeMillis,
                    artworkUrl100 = it.artworkUrl100,
                    collectionName = it.collectionName,
                    releaseDate = it.releaseDate,
                    primaryGenreName = it.primaryGenreName,
                    country = it.country,
                    previewUrl = it.previewUrl,
                    isFavorite = isFavorite
                ))
            }
        }

        return tracks

    }


    override suspend fun createPlaylist(playlist: Playlist): Long {
        val entity = PlaylistEntity(
            name = playlist.name,
            description = playlist.description,
            coverImagePath = playlist.coverImagePath,
            trackIdsJson = PlaylistEntity.createTrackIdsJson(playlist.trackIds),
            trackCount = playlist.trackCount
        )

        return database.playlistsDao().insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistEntity(
            playlistId = playlist.playlistId,
            name = playlist.name,
            description = playlist.description,
            coverImagePath = playlist.coverImagePath,
            trackIdsJson = PlaylistEntity.createTrackIdsJson(playlist.trackIds),
            trackCount = playlist.trackCount
        )
        database.playlistsDao().updatePlaylist(entity)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return database.playlistsDao().getAllPlaylists().map { entities ->
            entities.map { it.toPlaylist() }
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return database.playlistsDao().getPlaylistById(playlistId)?.toPlaylist()
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        database.playlistsDao().deletePlaylist(playlistId)
    }


    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Int): Boolean {
        val playlistEntity = database.playlistsDao().getPlaylistById(playlistId)
            ?: return false

        val trackIds = playlistEntity.getTrackIds()
        return trackIds.contains(trackId)
    }

    private fun PlaylistEntity.toPlaylist(): Playlist {
        return Playlist(
            playlistId = playlistId,
            name = name,
            description = description,
            coverImagePath = coverImagePath,
            trackIds = getTrackIds(),
            trackCount = trackCount
        )
    }
}