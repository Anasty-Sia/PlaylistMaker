package com.example.playlistmaker.library.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.adapters.PlaylistsAdapter
import com.example.playlistmaker.library.ui.view_model.PlaylistsState
import com.example.playlistmaker.library.ui.view_model.PlaylistsViewModel
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
        try {
            playlistsAdapter = PlaylistsAdapter {

            }

            val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            binding.recyclerViewPlaylists.layoutManager = gridLayoutManager
            binding.recyclerViewPlaylists.adapter = playlistsAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
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


        binding.createPlaylistButton.apply {

            setOnClickListener {
                try {
                    // Пытаемся использовать action
                    findNavController().navigate(R.id.action_libraryFragment_to_newPlaylistFragment)
                } catch (e: Exception) {
                    // Если action не найден, используем прямой переход
                    findNavController().navigate(R.id.newPlaylistFragment)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
