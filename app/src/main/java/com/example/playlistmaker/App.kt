package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.di.appModule
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App: Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        setupAppTheme()

    }

    private fun setupAppTheme() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val settingsInteractor = getKoin().get<SettingsInteractor>()
                val isDarkTheme = settingsInteractor.isDarkThemeEnabled()
                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}










