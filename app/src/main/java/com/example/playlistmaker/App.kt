package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.di.appModule
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        setupAppTheme()

    }
    private fun setupAppTheme() {
            try {

                val isDarkTheme =  runBlocking {
                    val settingsInteractor = getKoin().get<SettingsInteractor>()
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




