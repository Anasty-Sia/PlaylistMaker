package com.example.playlistmaker.search.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
    }

    fun searchDebounced(query: String) {
        searchJob?.cancel()

        if (query.isEmpty()) {
            loadSearchHistory()
            return
        }

        searchJob = viewModelScope.launch {
            _searchState.postValue(SearchState.Loading)
            delay(SEARCH_DEBOUNCE_DELAY)
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        try {
            searchInteractor.searchTracks(query)
                .flowOn(Dispatchers.IO)
                .collect { tracks ->
                    if (tracks.isEmpty()) {
                        _searchState.postValue(SearchState.Empty("Ничего не найдено"))
                    } else {
                        _searchState.postValue(SearchState.Content(tracks))
                    }
                }
        } catch (e: Exception){
            _searchState.postValue(SearchState.Error("Проверьте подключение к интернету"))
        }
    }

    fun addTrackToHistory(track: Track) {
        viewModelScope.launch {
            searchHistoryInteractor.addTrackToHistory(track)
        }
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            searchHistoryInteractor.getSearchHistory()
                .collect { history ->
                    if (history.isNotEmpty()) {
                        _searchState.postValue(SearchState.History(history))
                    } else {
                        _searchState.postValue(SearchState.Default)
                    }
                }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryInteractor.clearSearchHistory()
            _searchState.postValue(SearchState.Default)
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}

sealed class SearchState {
    object Default : SearchState()
    object Loading : SearchState()
    data class Content(val tracks: List<Track>) : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
    data class Empty(val message: String) : SearchState()
    data class Error(val message: String) : SearchState()
}