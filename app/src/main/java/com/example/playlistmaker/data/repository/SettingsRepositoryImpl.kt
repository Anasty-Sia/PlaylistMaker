package com.example.playlistmaker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    private val darkThemeKey = "dark_theme"

    override suspend fun setDarkThemeEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(darkThemeKey, enabled).apply()
    }

    override suspend fun isDarkThemeEnabled(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(darkThemeKey, false)
    }
}