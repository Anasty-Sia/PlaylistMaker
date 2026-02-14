package com.example.playlistmaker.library.ui.view_model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.launch

open class NewPlaylistViewModel(
    protected val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    protected val _playlistName = MutableLiveData<String?>()
    val playlistName: LiveData<String?> = _playlistName

    protected val _playlistDescription = MutableLiveData<String?>()
    val playlistDescription: LiveData<String?> = _playlistDescription

    protected val _coverImagePath = MutableLiveData<String?>()
    val coverImagePath: LiveData<String?> = _coverImagePath

    protected val _coverUri = MutableLiveData<Uri?>()
    val coverUri: LiveData<Uri?> = _coverUri

    protected var savedCoverImagePath: String? = null

    fun updatePlaylistName(name: String) {
        _playlistName.value = name
    }

    fun updatePlaylistDescription(description: String) {
        _playlistDescription.value = description
    }

    fun updateCoverUri(uri: Uri?) {
        _coverUri.value = uri
    }

    fun setSavedCoverPath(path: String?) {
        savedCoverImagePath = path
        _coverImagePath.value = path
    }

    open fun savePlaylist(onSuccess: (Long) -> Unit) {
        val name = _playlistName.value?.trim()
        if (name.isNullOrEmpty()) return

        viewModelScope.launch {
            val playlist = Playlist(
                name = name,
                description = _playlistDescription.value?.takeIf { it.isNotEmpty() },
                coverImagePath = savedCoverImagePath,
                trackIds = emptyList(),
                trackCount = 0
            )
            val id = playlistsInteractor.createPlaylist(playlist)
            onSuccess(id)
        }
    }

    fun isFormValid(): Boolean {
        return !_playlistName.value.isNullOrBlank()
    }
}