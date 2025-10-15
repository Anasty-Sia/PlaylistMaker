package com.example.playlistmaker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchHistoryRepositoryImpl(private val context: Context) : SearchHistoryRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
    private val gson = Gson()


    override suspend fun addTrackToHistory(track: Track) = withContext(Dispatchers.IO) {
        val history = getSearchHistory().toMutableList()

        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > maxSize) {
            history.removeAt(history.size - 1)
        }

        saveHistory(history)
    }

    override suspend fun getSearchHistory(): List<Track> = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(key, null)
        return@withContext if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson<List<Track>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    override suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(key).apply()
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