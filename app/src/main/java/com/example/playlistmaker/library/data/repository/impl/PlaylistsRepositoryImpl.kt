package com.example.playlistmaker.library.data.repository.impl

import android.content.Context
import com.example.playlistmaker.R
import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.data.db.PlaylistEntity
import com.example.playlistmaker.library.data.db.PlaylistTrackEntity
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.domain.repository.PlaylistsRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistsRepositoryImpl(
    private val database: AppDatabase,  private val context: Context
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
            previewUrl = track.previewUrl,
            addedDate = System.currentTimeMillis()
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
        if (trackIds.isEmpty()) return emptyList()

        val trackEntities = database.playlistTracksDao().getTracksByIds(trackIds)

        val trackMap = trackEntities.associateBy { it.trackId }
        val sortedTracks = trackIds.reversed().mapNotNull { trackMap[it] }

        val favoriteTrackIds = database.favoriteTracksDao().getAllFavoriteTrackIds()

        return sortedTracks.map { entity ->
            Track(
                trackId = entity.trackId,
                trackName = entity.trackName,
                artistName = entity.artistName,
                trackTimeMillis = entity.trackTimeMillis ?: 0L,
                artworkUrl100 = entity.artworkUrl100,
                collectionName = entity.collectionName,
                releaseDate = entity.releaseDate,
                primaryGenreName = entity.primaryGenreName,
                country = entity.country,
                previewUrl = entity.previewUrl,
                isFavorite = favoriteTrackIds.contains(entity.trackId)
            )
        }
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

    override fun getPlaylistByIdFlow(playlistId: Long): Flow<Playlist?> {
        return database.playlistsDao().getPlaylistByIdFlow(playlistId)
            .map { entity ->
                entity?.toPlaylist()
            }
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

    override suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Int) {
        val playlistEntity = database.playlistsDao().getPlaylistById(playlistId)
            ?: throw IllegalArgumentException("Playlist not found")

        val currentTrackIds = playlistEntity.getTrackIds()
        if (!currentTrackIds.contains(trackId)) {
            return
        }

        val updatedTrackIds = currentTrackIds.filter { it != trackId }
        val updatedEntity = playlistEntity.copy(
            trackIdsJson = PlaylistEntity.createTrackIdsJson(updatedTrackIds),
            trackCount = updatedTrackIds.size
        )

        database.playlistsDao().updatePlaylist(updatedEntity)

        cleanupOrphanedTracks()
    }

    override suspend fun cleanupOrphanedTracks() {
        val playlists = database.playlistsDao().getAllPlaylists().firstOrNull() ?: emptyList()

        val allTrackIdsInPlaylists = playlists
            .flatMap { it.getTrackIds() }
            .toSet()

        val allStoredTracks = database.playlistTracksDao().getAllTracks()

        allStoredTracks.forEach { trackEntity ->
            if (!allTrackIdsInPlaylists.contains(trackEntity.trackId)) {
                database.playlistTracksDao().deleteTrackById(trackEntity.trackId)
            }
        }
    }

    override suspend fun sharePlaylist(playlistId: Long): String? {
        val playlist = getPlaylistById(playlistId) ?: return null
        val tracks = getPlaylistTracks(playlistId)

        if (tracks.isEmpty()) return null

        return buildString {
            appendLine(playlist.name)
            if (!playlist.description.isNullOrEmpty()) {
                appendLine(playlist.description)
            }
            val trackCountText = context.resources.getQuantityString(
                R.plurals.tracks_count,
                tracks.size,
                tracks.size)
            appendLine(trackCountText)
            appendLine()

            tracks.forEachIndexed { index, track ->
                val duration = formatTrackTime(track.trackTimeMillis)
                appendLine("${index + 1}. ${track.artistName} - ${track.trackName} ($duration)")
            }
        }
    }

    override suspend fun deletePlaylistWithTracks(playlistId: Long) {
        deletePlaylist(playlistId)
        cleanupOrphanedTracks()
    }

    private fun formatTrackTime(timeMillis: Long?): String {
        return try {
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(timeMillis ?: 0)
        } catch (e: Exception) {
            "0:00"
        }
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