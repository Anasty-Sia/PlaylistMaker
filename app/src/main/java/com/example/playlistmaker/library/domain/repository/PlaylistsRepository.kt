package com.example.playlistmaker.library.domain.repository


import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track)
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Int): Boolean
    suspend fun getPlaylistTracks(playlistId: Long): List<Track>

    suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Int)
    suspend fun cleanupOrphanedTracks()

    suspend fun sharePlaylist(playlistId: Long): String?
    suspend fun deletePlaylistWithTracks(playlistId: Long)



}