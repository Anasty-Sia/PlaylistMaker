package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addTrack(track: Track) {
        val history = getHistory().toMutableList()

        history.removeAll { it.trackId == track.trackId }

        history.add(0, track)

        if (history.size > maxSize) {
            history.removeAt(history.size - 1)
        }

        saveHistory(history)
    }

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun clearHistory() {
        sharedPreferences.edit { remove(key) }
    }

    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit { putString(key, json) }
    }

    companion object {
        private const val key = "search_history"
        private const  val maxSize = 10

    }
}