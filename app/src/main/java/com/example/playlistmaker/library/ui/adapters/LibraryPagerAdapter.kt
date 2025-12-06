package com.example.playlistmaker.library.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.library.ui.fragments.FavoriteTracksFragment
import com.example.playlistmaker.library.ui.fragments.PlaylistsFragment

class LibraryPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> FavoriteTracksFragment()
            1 -> PlaylistsFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}