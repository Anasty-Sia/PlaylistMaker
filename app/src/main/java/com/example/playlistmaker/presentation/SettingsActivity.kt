package com.example.playlistmaker.presentation

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.domain.interactor.SettingsInteractorInterface
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsInteractor: SettingsInteractorInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsInteractor = Creator.provideSettingsInteractor(this)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupViews()
        loadCurrentTheme()
    }

    private fun setupSystemBars() {
        val backButton = findViewById<MaterialToolbar>(R.id.back_b)

        ViewCompat.setOnApplyWindowInsetsListener(backButton) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }
    }

    private fun setupViews() {
        binding.backB.setNavigationOnClickListener {
            finish()
        }

        binding.darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsInteractor.setDarkThemeEnabled(isChecked)
                setDarkTheme(isChecked)
            }
        }

        binding.iconShare.setOnClickListener {
            shareApp()
        }

        binding.iconSupport.setOnClickListener {
            sendSupportEmail()
        }

        binding.termsOfService.setOnClickListener {
            openTermsOfService()
        }
    }

    private fun loadCurrentTheme() {
        lifecycleScope.launch {
            val isDarkTheme = settingsInteractor.isDarkThemeEnabled()
            binding.darkThemeSwitch.isChecked = isDarkTheme
        }
    }

    private fun setDarkTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)))
    }

    private fun sendSupportEmail() {
        val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_support)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.email_message))
        }
        startActivity(supportIntent)
    }

    private fun openTermsOfService() {
        val termsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(getString(R.string.terms_of_service_url))
        }
        startActivity(termsIntent)
    }
}