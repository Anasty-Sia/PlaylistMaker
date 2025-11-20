package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App: Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        setupAppTheme()
    }

    private fun setupAppTheme() {
        applicationScope.launch {
            try {
                val settingsInteractor = Creator.provideSettingsInteractor(this@App)
                val isDarkTheme = settingsInteractor.isDarkThemeEnabled()

                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            } catch (e: Exception) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}


