package com.example.playlistmaker.search.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.search.ui.adapter.TrackAdapter
import com.example.playlistmaker.search.ui.view_model.SearchState
import com.example.playlistmaker.search.ui.view_model.SearchViewModel
import kotlinx.coroutines.Job
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.lifecycleScope
import com.example.playlistmaker.root.RootActivity
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(FlowPreview::class)
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var clickJob: Job? = null

    private var searchAdapter: TrackAdapter? = null
    private var historyAdapter: TrackAdapter? = null

    private val viewModel: SearchViewModel by viewModel()

    private val trackClickFlow = MutableSharedFlow<Track>(
        extraBufferCapacity = 1,
        replay = 0
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.init()

        trackClickFlow
            .debounce(CLICK_DEBOUNCE_DELAY)
            .onEach { track ->
                (activity as? RootActivity)?.animateBottomNavigationView()
                viewModel.addTrackToHistory(track)
                val action = SearchFragmentDirections.actionSearchFragmentToPlayerFragment(track)
                findNavController().navigate(action)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        searchAdapter = TrackAdapter(emptyList()) { track ->
            lifecycleScope.launch {
                trackClickFlow.emit(track)
            }
        }

        historyAdapter = TrackAdapter(emptyList()) { track ->
            (activity as? RootActivity)?.animateBottomNavigationView()
            lifecycleScope.launch {
                trackClickFlow.emit(track)
            }
        }

        setupStatusBarPadding()
        setupViews()
        setupSearchField()
        observeViewModel()
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

    private fun setupViews() {
        binding.rvTrack.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrack.adapter = searchAdapter

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }

        binding.refreshButton.setOnClickListener {
            val query = binding.inputSearch.text?.toString()
            if (!query.isNullOrEmpty()) {
                viewModel.searchDebounced(query)
            }
        }
    }

    private fun setupSearchField() {
        binding.inputSearch.doOnTextChanged { text, _, _, _ ->
            val searchText = text?.toString() ?: ""
            binding.clearIcon.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
            viewModel.searchDebounced(searchText)
        }

        binding.clearIcon.setOnClickListener {
            binding.inputSearch.text?.clear()
            viewModel.loadSearchHistory()

        }
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Default -> showDefaultState()
                is SearchState.Loading -> showLoading()
                is SearchState.Empty -> showEmptyState(state.message)
                is SearchState.Error -> showErrorState(state.message)
                is SearchState.Content -> showResults(state.tracks)
                is SearchState.History -> showSearchHistory(state.tracks)
            }
        }
    }


    private fun showDefaultState() {
        binding.historyContainer.visibility = View.GONE
        binding.rvTrack.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.historyContainer.visibility = View.GONE
        binding.rvTrack.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showResults(tracks: List<Track>) {
        binding.historyContainer.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.rvTrack.visibility = View.VISIBLE
        searchAdapter?.updateData(tracks)
    }

    private fun showSearchHistory(tracks: List<Track>) {
        binding.rvTrack.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.VISIBLE
        historyAdapter?.updateData(tracks)

    }

    private fun showEmptyState(message: String) {
        binding.historyContainer.visibility = View.GONE
        binding.rvTrack.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.emptyResults.visibility = View.VISIBLE
        binding.emptyResultsText.text = message
    }

    private fun showErrorState(message: String) {
        binding.historyContainer.visibility = View.GONE
        binding.rvTrack.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
        binding.errorText.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clickJob?.cancel()
        searchAdapter = null
        historyAdapter = null
        binding.rvTrack.adapter = null
        binding.rvHistory.adapter = null
        _binding = null
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 300L
    }
}