package com.example.playlistmaker.library.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.library.ui.adapters.LibraryPagerAdapter
import com.example.playlistmaker.databinding.FragmentLibraryBinding
import com.google.android.material.tabs.TabLayoutMediator

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var tabMediator: TabLayoutMediator

    private var selectedTab: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPagerAndTabs()
        setupStatusBarPadding()

        savedInstanceState?.getInt("SELECTED_TAB")?.let {
            selectedTab = it
            binding.viewPager.currentItem = it
        }
    }

    private fun setupStatusBarPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.backB.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }

            insets
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.let {
            outState.putInt("SELECTED_TAB", it.viewPager.currentItem)
        } ?: run {
            outState.putInt("SELECTED_TAB", selectedTab)
        }
    }

    private fun setupViewPagerAndTabs() {
        binding.viewPager.adapter = LibraryPagerAdapter(requireActivity() as FragmentActivity)

        val tabTitles = listOf(
            getString(R.string.favorite_tracks),
            getString(R.string.playlists)
        )

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }
        tabMediator.attach()

        binding.viewPager.currentItem = selectedTab
    }

    override fun onDestroyView() {
        _binding?.let {
            selectedTab = it.viewPager.currentItem
        }

        if (::tabMediator.isInitialized) {
            tabMediator.detach()
        }
        _binding = null
        super.onDestroyView()
    }
}