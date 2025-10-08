package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.SettingsRepository

class SettingsInteractor(private val settingsRepository: SettingsRepository) {
    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        settingsRepository.setDarkThemeEnabled(enabled)
    }

    suspend fun isDarkThemeEnabled(): Boolean {
        return settingsRepository.isDarkThemeEnabled()
    }
}