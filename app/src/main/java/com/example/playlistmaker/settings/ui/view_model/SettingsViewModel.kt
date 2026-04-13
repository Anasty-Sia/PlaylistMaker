package com.example.playlistmaker.settings.ui.view_model

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.settings.domain.interactor.SharingInteractor

import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor,
    private val sharingInteractor: SharingInteractor
) : ViewModel() {

    private val _darkThemeEnabled = MutableLiveData<Boolean>(false)
    val darkThemeEnabled: LiveData<Boolean> = _darkThemeEnabled

    init {
        loadThemeSetting()
    }

      fun loadThemeSetting() {
        viewModelScope.launch {
            try {
                val isDarkTheme = settingsInteractor.isDarkThemeEnabled()
                _darkThemeEnabled.value= isDarkTheme
                applyTheme(isDarkTheme)
            } catch (e: Exception) {
                e.printStackTrace()
                _darkThemeEnabled.postValue(false)
            }
        }
      }



    fun onThemeSwitchChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsInteractor.setDarkThemeEnabled(enabled)
            _darkThemeEnabled.postValue(enabled)
            applyTheme(enabled)
        }
    }


    private fun applyTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun onShareClicked() {
        sharingInteractor.shareApp()
    }

    fun onSupportClicked() {
        sharingInteractor.openSupport()
    }

    fun onTermsClicked() {
        sharingInteractor.openTerms()
    }
}

