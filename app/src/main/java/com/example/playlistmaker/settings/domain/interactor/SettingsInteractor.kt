package com.example.playlistmaker.settings.domain.interactor

import com.example.playlistmaker.settings.data.repository.SettingsRepository

class SettingsInteractor(private val settingsRepository: SettingsRepository):
    SettingsRepository {
    override suspend fun setDarkThemeEnabled(enabled: Boolean) {
        settingsRepository.setDarkThemeEnabled(enabled)
    }

    override suspend fun isDarkThemeEnabled(): Boolean {
        return settingsRepository.isDarkThemeEnabled()
    }
}