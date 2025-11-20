package com.example.playlistmaker.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.playlistmaker.databinding.ActivityMainBinding
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.search.ui.activity.SearchActivity
import com.example.playlistmaker.settings.ui.activity.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupViews()
    }


    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
                bottomMargin = systemBars.bottom
            }
            insets
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

