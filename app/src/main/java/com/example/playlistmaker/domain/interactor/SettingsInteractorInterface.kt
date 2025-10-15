package com.example.playlistmaker.domain.interactor

interface SettingsInteractorInterface {
    suspend fun setDarkThemeEnabled(enabled: Boolean)
    suspend fun isDarkThemeEnabled(): Boolean
}