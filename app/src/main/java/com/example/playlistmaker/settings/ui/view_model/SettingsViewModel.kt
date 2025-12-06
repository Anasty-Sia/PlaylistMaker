package com.example.playlistmaker.settings.ui.view_model

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

    private val _darkThemeEnabled = MutableLiveData<Boolean>()
    val darkThemeEnabled: LiveData<Boolean> = _darkThemeEnabled

    init {
        loadThemeSetting()
    }

      fun loadThemeSetting() {
        viewModelScope.launch {
            try {
                val isDarkTheme = settingsInteractor.isDarkThemeEnabled()
                _darkThemeEnabled.value= isDarkTheme
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
        }
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

