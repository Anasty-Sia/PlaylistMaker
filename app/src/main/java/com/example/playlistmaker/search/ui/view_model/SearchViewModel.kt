package com.example.playlistmaker.search.ui.view_model


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Default)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
        Log.d("SearchViewModel", "ViewModel created")
    }


    fun searchDebounced(query: String) {
        searchJob?.cancel()
        Log.d("SearchViewModel", "searchDebounced: '$query'")

        if (query.isEmpty()) {
            Log.d("SearchViewModel", "Empty query, loading history")
            loadSearchHistory()
            return
        }

        searchJob = viewModelScope.launch {
            _searchState.update {SearchState.Loading}
            delay(SEARCH_DEBOUNCE_DELAY)
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        try {
            Log.d("SearchViewModel", "performSearch: '$query'")
            val tracks = searchInteractor.searchTracks(query)
            Log.d("SearchViewModel", "Received ${tracks.size} tracks")
            if (tracks.isEmpty()) {
                _searchState.value = SearchState.Empty("Ничего не найдено")
            } else {
                _searchState.value = SearchState.Content(tracks)
            }
        } catch (e: Exception){
            Log.e("SearchViewModel", "Search error", e)
            _searchState.update { SearchState.Error("Проверьте подключение к интернету")}
        }
    }

    fun addTrackToHistory(track: Track) {
        viewModelScope.launch {
            searchHistoryInteractor.addTrackToHistory(track)
        }
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            Log.d("SearchViewModel", "Loading search history")
            val history = searchHistoryInteractor.loadHistoryAndGet()
            Log.d("SearchViewModel", "History size: ${history.size}")
            if (history.isNotEmpty()) {
                _searchState.value = SearchState.History(history)
            } else {
                _searchState.value = SearchState.Default
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryInteractor.clearSearchHistory()
            _searchState.update { SearchState.Default}
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