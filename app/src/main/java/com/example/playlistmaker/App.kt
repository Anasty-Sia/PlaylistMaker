package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.runBlocking

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        setupAppTheme()
    }
    private fun setupAppTheme() {
            try {
                val settingsInteractor = Creator.provideSettingsInteractor(this)
                val isDarkTheme =  runBlocking {
                    settingsInteractor.isDarkThemeEnabled()
                }

                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            } catch (e: Exception) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }




