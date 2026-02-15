package com.example.playlistmaker.library.domain.interactor.impl

import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.domain.repository.PlaylistsRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class PlaylistsInteractorImpl(
    private val repository: PlaylistsRepository
) : PlaylistsInteractor {

    override suspend fun createPlaylist(playlist: Playlist): Long {
        return repository.createPlaylist(playlist)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return repository.getPlaylistById(playlistId)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        repository.deletePlaylist(playlistId)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        repository.addTrackToPlaylist(playlistId, track)
    }

    override suspend fun getPlaylistTracks(playlistId: Long): List<Track> {
        return repository.getPlaylistTracks(playlistId)
    }

    override suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Int) {
        repository.deleteTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun sharePlaylist(playlistId: Long): String? {
        return repository.sharePlaylist(playlistId)
    }

    override suspend fun deletePlaylistWithTracks(playlistId: Long) {
        repository.deletePlaylistWithTracks(playlistId)
    }

    override fun getPlaylistByIdFlow(playlistId: Long): Flow<Playlist?> {
        return repository.getPlaylistByIdFlow(playlistId)
    }

}