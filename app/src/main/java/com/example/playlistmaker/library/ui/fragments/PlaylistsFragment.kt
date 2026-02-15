package com.example.playlistmaker.library.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.library.ui.adapters.PlaylistsAdapter
import com.example.playlistmaker.library.ui.view_model.PlaylistsState
import com.example.playlistmaker.library.ui.view_model.PlaylistsViewModel
import com.example.playlistmaker.root.RootActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()


    private lateinit var playlistsAdapter: PlaylistsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()
        setupCreatePlaylistButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        playlistsAdapter = PlaylistsAdapter { playlist ->
            try {
                val bundle = Bundle().apply {
                    putLong("playlistId", playlist.playlistId)  // Важно: используем тот же ключ
                }
                findNavController().navigate(R.id.playlistDetailsFragment, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewPlaylists.layoutManager = gridLayoutManager
        binding.recyclerViewPlaylists.adapter = playlistsAdapter
    }


    private fun observeViewModel() {
        viewModel.playlistsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistsState.Empty -> showEmptyState()
                is PlaylistsState.Content -> showPlaylists(state.playlists)
            }
        }
    }

    private fun showEmptyState() {
        binding.emptyStateView.visibility = View.VISIBLE
        binding.recyclerViewPlaylists.visibility = View.GONE
        binding.createPlaylistButton.visibility = View.VISIBLE
        playlistsAdapter.updateData(emptyList())
    }

    private fun showPlaylists(playlists: List<com.example.playlistmaker.library.domain.model.Playlist>) {
        binding.emptyStateView.visibility = View.GONE
        binding.recyclerViewPlaylists.visibility = View.VISIBLE
        binding.createPlaylistButton.visibility = View.VISIBLE

        playlistsAdapter.updateData(playlists)
    }


    private fun setupCreatePlaylistButton() {

        binding.createPlaylistButton.setOnClickListener {
            try {
                findNavController().navigate(R.id.newPlaylistFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? RootActivity)?.showBottomNavigationView()
        _binding = null
    }
}
