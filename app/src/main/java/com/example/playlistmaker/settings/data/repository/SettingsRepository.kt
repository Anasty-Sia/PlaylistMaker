package com.example.playlistmaker.settings.data.repository

interface SettingsRepository {
    suspend fun setDarkThemeEnabled(enabled: Boolean)
    suspend fun isDarkThemeEnabled(): Boolean
}