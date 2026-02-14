package com.example.playlistmaker.library.domain.interactor

import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    suspend fun createPlaylist(playlist: Playlist): Long
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long)

    suspend fun addTrackToPlaylist(playlistId: Long, track: Track)
    suspend fun getPlaylistTracks(playlistId: Long): List<Track>

    suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Int)

    suspend fun sharePlaylist(playlistId: Long): String?
    suspend fun deletePlaylistWithTracks(playlistId: Long)



}
