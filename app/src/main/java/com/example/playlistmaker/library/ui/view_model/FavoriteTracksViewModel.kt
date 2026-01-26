package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
): ViewModel() {

    private val _favoriteTracksState = MutableLiveData<FavoriteTracksState>()
    val favoriteTracksState: LiveData<FavoriteTracksState> = _favoriteTracksState

    init {
        loadFavoriteTracks()
    }

    fun loadFavoriteTracks() {
        viewModelScope.launch {
                favoriteTracksInteractor.getFavoriteTracks().collect { tracks ->
                    if (tracks.isEmpty()) {
                        _favoriteTracksState.postValue(FavoriteTracksState.Empty)
                    } else {
                        _favoriteTracksState.postValue(FavoriteTracksState.Content(tracks))
                    }
                }
        }
    }

    fun refresh() {
        loadFavoriteTracks()
    }
}

sealed class FavoriteTracksState {
    object Empty : FavoriteTracksState()
    data class Content(val tracks: List<Track>) : FavoriteTracksState()

}