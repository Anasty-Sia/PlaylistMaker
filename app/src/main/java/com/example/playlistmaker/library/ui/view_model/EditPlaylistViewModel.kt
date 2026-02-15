package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditPlaylistUiState(
    val playlist: Playlist? = null,
    val name: String = "",
    val description: String = "",
    val coverImagePath: String? = null,
    val isFormValid: Boolean = false,
    val isUpdating: Boolean = false,
    val updateResult: Boolean? = null,
    val error: String? = null
)

class EditPlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    playlistId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPlaylistUiState())
    val uiState: StateFlow<EditPlaylistUiState> = _uiState.asStateFlow()

    init {
        loadPlaylist(playlistId)
    }

    private fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                playlistsInteractor.getPlaylistByIdFlow(playlistId).collect { playlist ->
                    playlist?.let {
                        _uiState.update { state ->
                            state.copy(
                                playlist = it,
                                name = it.name,
                                description = it.description ?: "",
                                coverImagePath = it.coverImagePath
                            )
                        }
                        validateForm(it.name)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка загрузки плейлиста") }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
        validateForm(name)
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateCoverImagePath(path: String?) {
        _uiState.update { it.copy(coverImagePath = path) }
    }

    private fun validateForm(name: String) {
        val isValid = name.isNotBlank()
        _uiState.update { it.copy(isFormValid = isValid) }
    }

    fun updatePlaylist(onComplete: (Boolean) -> Unit) {
        val currentState = _uiState.value
        val playlist = currentState.playlist ?: return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isUpdating = true) }

                val updatedPlaylist = Playlist(
                    playlistId = playlist.playlistId,
                    name = currentState.name.trim(),
                    description = currentState.description.takeIf { it.isNotBlank() },
                    coverImagePath = currentState.coverImagePath ?: playlist.coverImagePath,
                    trackIds = playlist.trackIds,
                    trackCount = playlist.trackCount
                )

                playlistsInteractor.updatePlaylist(updatedPlaylist)

                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        updateResult = true
                    )
                }

                withContext(Dispatchers.Main) {
                    onComplete(true)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        updateResult = false,
                        error = "Ошибка при обновлении плейлиста"
                    )
                }
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    fun hasChanges(): Boolean {
        val state = _uiState.value
        val original = state.playlist ?: return false

        return state.name != original.name ||
                state.description.takeIf { it.isNotBlank() } != original.description ||
                state.coverImagePath != original.coverImagePath
    }

    fun clearUpdateResult() {
        _uiState.update { it.copy(updateResult = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

