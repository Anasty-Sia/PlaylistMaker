package com.example.playlistmaker.settings.domain.interactor

interface SettingsInteractorInterface {
    suspend fun setDarkThemeEnabled(enabled: Boolean)
    suspend fun isDarkThemeEnabled(): Boolean
}