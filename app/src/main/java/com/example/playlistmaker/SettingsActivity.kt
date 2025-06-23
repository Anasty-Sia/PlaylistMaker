package com.example.playlistmaker

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            enableEdgeToEdge()
            setContentView(R.layout.activity_settings)

            val backButton = findViewById<MaterialToolbar>(R.id.back_b)

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
        }

}

