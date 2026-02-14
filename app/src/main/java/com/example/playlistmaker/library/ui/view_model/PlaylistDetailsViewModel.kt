package com.example.playlistmaker.library.ui.view_model

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean


data class PlaylistDetailsState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val totalDuration: String = "",
    val isLoading: Boolean = false
)

class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }

    @MainThread
    override fun setValue(value: T?) {
        pending.set(true)
        super.setValue(value)
    }
}

class PlaylistDetailsViewModel(
    private val playlistsInteractor: PlaylistsInteractor, private val applicationContext: Context
) : ViewModel() {

    private val _state = MutableLiveData(PlaylistDetailsState(isLoading = true))
    val state: LiveData<PlaylistDetailsState> = _state

    // Разовые события
    private val _shareResult = SingleLiveEvent<String?>()
    val shareResult: LiveData<String?> = _shareResult

    private val _showEmptyPlaylistToast = SingleLiveEvent<Boolean>()
    val showEmptyPlaylistToast: LiveData<Boolean> = _showEmptyPlaylistToast

    private val _navigateBack = SingleLiveEvent<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    private var currentPlaylistId: Long = 0L
    private var loadPlaylistJob: Job? = null
    private var loadTracksJob: Job? = null


    fun loadPlaylist(playlistId: Long) {
        currentPlaylistId = playlistId

        loadPlaylistJob?.cancel()
        loadPlaylistJob = viewModelScope.launch {
            try {
                val playlist = playlistsInteractor.getPlaylistById(playlistId)
                _state.value = _state.value?.copy(playlist = playlist, isLoading = false)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value?.copy(playlist = null, isLoading = false)
            }
        }
    }

    fun loadPlaylistTracks(playlistId: Long) {
        loadTracksJob?.cancel()

        loadTracksJob = viewModelScope.launch {
            _state.value = _state.value?.copy(isLoading = true)
            try {
                val tracks = playlistsInteractor.getPlaylistTracks(playlistId)
                withContext(Dispatchers.Main) {
                    val duration = formatDuration(tracks)
                    _state.value = _state.value?.copy(
                        tracks = tracks,
                        totalDuration = duration,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = _state.value?.copy(
                        tracks = emptyList(),
                        totalDuration = formatDuration(emptyList()),
                        isLoading = false
                    )
                }
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


                } else {
                    _shareResult.value = shareText
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deletePlaylist() {
        if (currentPlaylistId == 0L) return

        viewModelScope.launch {
            try {
                playlistsInteractor.deletePlaylistWithTracks(currentPlaylistId)
                _navigateBack.value = true
            } catch (e: Exception) {
                e.printStackTrace()
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

    private fun formatDuration(tracks: List<Track>): String {
        val totalMillis = tracks.sumOf { it.trackTimeMillis ?: 0L }
        if (totalMillis == 0L) {
            return applicationContext.resources.getQuantityString(R.plurals.minutes_count, 0, 0)
        }
        val minutes = totalMillis / 60000
        return applicationContext.resources.getQuantityString(R.plurals.minutes_count, minutes.toInt(), minutes)
    }

    override fun onCleared() {
        super.onCleared()
        loadPlaylistJob?.cancel()
        loadTracksJob?.cancel()
    }
}