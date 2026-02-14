package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditPlaylistViewModel(
    playlistsInteractor: PlaylistsInteractor,
    private val originalPlaylist: Playlist?
) : NewPlaylistViewModel(playlistsInteractor) {

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> = _updateResult

    private val _isUpdating = MutableLiveData(false)
    val isUpdating: LiveData<Boolean> = _isUpdating

    init {
        originalPlaylist?.let { playlist ->
            _playlistName.value = playlist.name
            _playlistDescription.value = playlist.description
            _coverImagePath.value = playlist.coverImagePath
            savedCoverImagePath = playlist.coverImagePath
        }
    }

    private val _isFormValid = MutableLiveData<Boolean>()
    val isFormValid: LiveData<Boolean> = _isFormValid

    fun validateForm(name: String) {
        _isFormValid.value = !name.isNullOrBlank()
    }


    fun updatePlaylist(
        name: String,
        description: String?,
        coverImagePath: String?,
        onComplete: (Boolean) -> Unit
    ) {
        if (originalPlaylist == null) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            try {
                _isUpdating.value = true

                val updatedPlaylist = Playlist(
                    playlistId = originalPlaylist.playlistId,
                    name = name,
                    description = description,
                    coverImagePath = coverImagePath ?: originalPlaylist.coverImagePath,
                    trackIds = originalPlaylist.trackIds,
                    trackCount = originalPlaylist.trackCount
                )

                playlistsInteractor.updatePlaylist(updatedPlaylist)

                _playlistName.value = name
                _playlistDescription.value = description
                _coverImagePath.value = coverImagePath ?: originalPlaylist.coverImagePath
                savedCoverImagePath = coverImagePath ?: originalPlaylist.coverImagePath

                _updateResult.postValue(true)

                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _updateResult.postValue(false)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            } finally {
                _isUpdating.postValue(false)
            }
        }
    }
    fun hasChanges(name: String, description: String?, coverPath: String?): Boolean {
        return originalPlaylist?.let { original ->
            name != original.name ||
                    description != original.description ||
                    coverPath != original.coverImagePath
        } ?: false
    }

    fun getOriginalPlaylistId(): Long {
        return originalPlaylist?.playlistId ?: 0L
    }

    fun getOriginalTrackIds(): List<Int> {
        return originalPlaylist?.trackIds ?: emptyList()
    }

    fun getOriginalTrackCount(): Int {
        return originalPlaylist?.trackCount ?: 0
    }
}