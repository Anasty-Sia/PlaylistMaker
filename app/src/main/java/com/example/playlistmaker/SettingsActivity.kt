package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import androidx.core.net.toUri
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)


            enableEdgeToEdge()
            setContentView(R.layout.activity_settings)

            val backButton = findViewById<MaterialToolbar>(R.id.back_b)
            val themeSwitch = findViewById<SwitchMaterial>(R.id.dark_theme_switch)

            ViewCompat.setOnApplyWindowInsetsListener(backButton) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = systemBars.top
                }
                insets
            }

            backButton.setNavigationOnClickListener {
                finish()
            }


            val shareTextView = findViewById<MaterialTextView>(R.id.icon_share)

            shareTextView.setOnClickListener{
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)))
            }


            val supportTextView = findViewById<MaterialTextView>(R.id.icon_support)
            supportTextView.setOnClickListener {
                val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:".toUri()
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_support)))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.email_message))
                }
                startActivity(supportIntent)
            }

            val termsOfServiceTextView = findViewById<MaterialTextView>(R.id.terms_of_service)
            termsOfServiceTextView.setOnClickListener{
                val termsIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = getString(R.string.terms_of_service_url).toUri()
                }
                startActivity(termsIntent)
            }


            // Восстановление состояния переключателя
            val sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
            val isDarkTheme = sharedPrefs.getBoolean(DARK_THEME_KEY, false)

            if (isDarkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            themeSwitch.isChecked = isDarkTheme

            // Обработчик переключения темы
            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                setDarkTheme(isChecked)
                saveThemePreference(isChecked)
            }
        }

        private fun setDarkTheme(enabled: Boolean) {
            AppCompatDelegate.setDefaultNightMode(
                if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        private fun saveThemePreference(enabled: Boolean) {
            getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
                .edit {
                    putBoolean(DARK_THEME_KEY, enabled)
                }
        }

    companion object {
        private const val SHARED_PREFS_NAME = "AppSettings"
        private const val DARK_THEME_KEY = "dark_theme"
    }

}
