package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
): ViewModel() {

    private val _favoriteTracksState = MutableStateFlow<FavoriteTracksState>(FavoriteTracksState.Empty)
    val favoriteTracksState: StateFlow<FavoriteTracksState> = _favoriteTracksState.asStateFlow()

    fun loadFavoriteTracks() {
        viewModelScope.launch {
            val tracks = favoriteTracksInteractor.getAllFavoriteTracks()
            if (tracks.isEmpty()) {
                _favoriteTracksState.value = FavoriteTracksState.Empty
            } else {
                _favoriteTracksState.value = FavoriteTracksState.Content(tracks)
            }
        }
    }

    fun refresh() {
        loadFavoriteTracks()
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            favoriteTracksInteractor.toggleFavorite(track)
            refresh()
        }
    }

    suspend fun isFavorite(trackId: Int): Boolean {
        return favoriteTracksInteractor.isFavorite(trackId)
    }
}

sealed class FavoriteTracksState {
    object Empty : FavoriteTracksState()
    data class Content(val tracks: List<Track>) : FavoriteTracksState()

}