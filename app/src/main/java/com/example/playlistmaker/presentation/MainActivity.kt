package com.example.playlistmaker.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.Creator
import com.example.playlistmaker.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsInteractor: com.example.playlistmaker.domain.interactor.SettingsInteractor
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsInteractor = Creator.provideSettingsInteractor(this)

        coroutineScope.launch {
            val isDarkTheme = settingsInteractor.isDarkThemeEnabled()
            setTheme(isDarkTheme)
        }

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setTheme(isDarkTheme: Boolean) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setupViews() {
        binding.searchB.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.libraryB.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }

        binding.settingsB.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}