package com.example.playlistmaker.library.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentFavoriteTracksBinding
import com.example.playlistmaker.library.ui.view_model.FavoriteTracksState
import com.example.playlistmaker.library.ui.view_model.FavoriteTracksViewModel
import androidx.lifecycle.lifecycleScope
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.adapter.TrackAdapter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(FlowPreview::class)
class FavoriteTracksFragment : Fragment() {

    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteTracksViewModel by viewModel()
    private var trackAdapter: TrackAdapter? = null

    private val trackClickFlow = MutableSharedFlow<Track>(
        extraBufferCapacity = 1,
        replay = 0
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackClickFlow
            .debounce(CLICK_DEBOUNCE_DELAY)
            .onEach { track ->
                navigateToPlayer(track)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        setupRecyclerView()
        observeViewModel()
    }

    private fun navigateToPlayer(track: Track) {
        val bundle = Bundle().apply {
            putParcelable("track", track)
        }
        findNavController().navigate(R.id.playerFragment, bundle)

    }


    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter(emptyList()) { track ->
            lifecycleScope.launch {
                trackClickFlow.emit(track)
            }
        }

        binding.recyclerViewTracks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTracks.adapter = trackAdapter
    }



    private fun observeViewModel() {
        viewModel.favoriteTracksState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoriteTracksState.Empty -> showEmptyState()
                is FavoriteTracksState.Content -> showTracks(state.tracks)
            }
        }
    }


    private fun showEmptyState() {
        binding.emptyStateView.visibility = View.VISIBLE
        binding.recyclerViewTracks.visibility = View.GONE
    }

    private fun showTracks(tracks: List<Track>) {
        binding.emptyStateView.isVisible = false
        binding.recyclerViewTracks.visibility = View.VISIBLE
        trackAdapter?.updateData(tracks)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        trackAdapter = null
        _binding = null
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 300L
    }
}