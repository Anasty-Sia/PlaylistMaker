package com.example.playlistmaker.settings.ui.activity


import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupViews()
        observeViewModel()
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
            viewModel.onThemeSwitchChanged(isChecked)
        }

        binding.iconShare.setOnClickListener {
            viewModel.onShareClicked()
        }

        binding.iconSupport.setOnClickListener {
            viewModel.onSupportClicked()
        }

        binding.termsOfService.setOnClickListener {
            viewModel.onTermsClicked()
        }
    }

    private fun observeViewModel() {
        viewModel.darkThemeEnabled.observe(this) { isEnabled ->
            binding.darkThemeSwitch.isChecked = isEnabled
            setDarkTheme(isEnabled)
        }
    }

    private fun setDarkTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
