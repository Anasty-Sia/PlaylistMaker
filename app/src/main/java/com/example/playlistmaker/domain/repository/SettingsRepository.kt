package com.example.playlistmaker.domain.repository

interface SettingsRepository {
    suspend fun setDarkThemeEnabled(enabled: Boolean)
    suspend fun isDarkThemeEnabled(): Boolean
}