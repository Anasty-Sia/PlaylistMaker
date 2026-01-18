package com.example.playlistmaker.search.data.repository.impl

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class SearchHistoryRepositoryImpl(private val context: Context,
                                  private val gson: Gson ) : SearchHistoryRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)

    private val _historyFlow = MutableStateFlow<List<Track>>(emptyList())

    init {

        loadHistory()
    }


    override suspend fun addTrackToHistory(track: Track) = withContext(Dispatchers.IO) {
        val history = getHistoryFromStorage().toMutableList()

        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > maxSize) {
            history.removeAt(history.size - 1)
        }

        saveHistory(history)
        _historyFlow.value = history
    }

    override fun getSearchHistory(): Flow<List<Track>>{
        return _historyFlow
    }

    override suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(key).apply()
        _historyFlow.value = emptyList()
    }

    private fun loadHistory() {
        val history = getHistoryFromStorage()
        _historyFlow.value = history
    }

    private fun getHistoryFromStorage(): List<Track> {
        val json = sharedPreferences.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson<List<Track>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(key, json).apply()
    }


    companion object {
        private const val key = "search_history"
        private const val maxSize = 10
    }
}