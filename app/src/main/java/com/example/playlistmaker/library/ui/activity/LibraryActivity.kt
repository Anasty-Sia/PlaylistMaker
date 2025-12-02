package com.example.playlistmaker.library.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.playlistmaker.R
import com.example.playlistmaker.library.ui.adapters.LibraryPagerAdapter
import com.example.playlistmaker.databinding.ActivityLibraryBinding
import com.google.android.material.tabs.TabLayoutMediator


class LibraryActivity : AppCompatActivity() {

    private var selectedTabPosition = 0
    private lateinit var binding: ActivityLibraryBinding
    private lateinit var tabMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (savedInstanceState != null) {
            selectedTabPosition = savedInstanceState.getInt("SELECTED_TAB", 0)
        }

        enableEdgeToEdge()
        setupEdgeToEdgeInsets()
        setupToolbar()
        setupViewPagerAndTabs()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SELECTED_TAB", binding.viewPager.currentItem)
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.backLibrary) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }
    }

    private fun setupToolbar() {
        binding.backLibrary.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewPagerAndTabs() {
        binding.viewPager.adapter = LibraryPagerAdapter(this)

        val tabTitles = listOf(
            getString(R.string.favorite_tracks),
            getString(R.string.playlists)
        )

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }
        tabMediator.attach()
    }


    override fun onDestroy() {
        tabMediator.detach()
        super.onDestroy()
    }
}