package com.example.playlistmaker.root

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import com.example.playlistmaker.ui.navigation.PlaylistMakerNavHost
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme

import org.koin.androidx.compose.koinViewModel


class RootActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val isDarkTheme by settingsViewModel.darkThemeEnabled.observeAsState(initial = false)

            PlaylistMakerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlaylistMakerNavHost()
                }
            }
        }
    }
}

