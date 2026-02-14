package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistDetailsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _playlist = MutableLiveData<Playlist?>()
    val playlist: LiveData<Playlist?> = _playlist

    private val _playlistTracks = MutableLiveData<List<Track>>()
    val playlistTracks: LiveData<List<Track>> = _playlistTracks

    private val _totalDuration = MutableLiveData<String>("0 минут")
    val totalDuration: LiveData<String> = _totalDuration

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _shareResult = MutableLiveData<String?>()
    val shareResult: LiveData<String?> = _shareResult

    private val _playlistDeleted = MutableLiveData(false)
    val playlistDeleted: LiveData<Boolean> = _playlistDeleted

    private val _showEmptyPlaylistToast = MutableLiveData(false)
    val showEmptyPlaylistToast: LiveData<Boolean> = _showEmptyPlaylistToast

    private var currentPlaylistId: Long = 0L
    private var loadPlaylistJob: Job? = null
    private var loadTracksJob: Job? = null

    fun loadPlaylist(playlistId: Long) {
        currentPlaylistId = playlistId

        loadPlaylistJob?.cancel()
        loadPlaylistJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                val playlist = playlistsInteractor.getPlaylistById(playlistId)
                _playlist.value = playlist
            } catch (e: Exception) {
                e.printStackTrace()
                _playlist.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPlaylistTracks(playlistId: Long) {
        loadTracksJob?.cancel()

        loadTracksJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                val tracks = playlistsInteractor.getPlaylistTracks(playlistId)
                withContext(Dispatchers.Main) {
                    _playlistTracks.value = tracks
                    calculateTotalDuration(tracks)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _playlistTracks.value = emptyList()
                    _totalDuration.value = "0 минут"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sharePlaylist() {
        if (currentPlaylistId == 0L) return

        viewModelScope.launch {
            try {
                val shareText = playlistsInteractor.sharePlaylist(currentPlaylistId)
                if (shareText == null) {
                    _showEmptyPlaylistToast.value = true
                    _shareResult.value = null
                } else {
                    _shareResult.value = shareText
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _shareResult.value = null
            }
        }
    }

    fun deletePlaylist() {
        if (currentPlaylistId == 0L) return

        viewModelScope.launch {
            try {
                playlistsInteractor.deletePlaylistWithTracks(currentPlaylistId)
                _playlistDeleted.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _playlistDeleted.value = false
            }
        }
    }

    fun deleteTrackFromPlaylist(trackId: Int) {
        viewModelScope.launch {
            try {
                playlistsInteractor.deleteTrackFromPlaylist(currentPlaylistId, trackId)
                loadPlaylistTracks(currentPlaylistId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetShowEmptyPlaylistToast() {
        _showEmptyPlaylistToast.value = false
    }

    private fun calculateTotalDuration(tracks: List<Track>) {
        val totalMillis = tracks.sumOf { it.trackTimeMillis ?: 0L }
        _totalDuration.value = formatDuration(totalMillis)
    }


    private fun formatDuration(durationMillis: Long): String {
        if (durationMillis == 0L) return "0 минут"
        val minutes = durationMillis / 60000
        return when {
            minutes == 0L -> "0 минут"
            minutes % 10 == 1L && minutes % 100 != 11L -> "$minutes минута"
            minutes % 10 in 2..4 && minutes % 100 !in 12..14 -> "$minutes минуты"
            else -> "$minutes минут"
        }
    }

    override fun onCleared() {
        super.onCleared()
        loadPlaylistJob?.cancel()
        loadTracksJob?.cancel()
    }
}