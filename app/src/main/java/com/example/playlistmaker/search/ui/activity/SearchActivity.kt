package com.example.playlistmaker.search.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.Creator
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.search.ui.adapter.TrackAdapter
import com.example.playlistmaker.search.ui.view_model.SearchState
import com.example.playlistmaker.search.ui.view_model.SearchViewModel


class SearchActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels {
        Creator.provideSearchViewModelFactory(this)
    }

    private val searchAdapter = TrackAdapter(emptyList()) { track -> onTrackClick(track) }
    private val historyAdapter = TrackAdapter(emptyList()) { track -> onTrackClick(track) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupViews()
        setupSearchField()
        observeViewModel()
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.backSearch) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }
    }

    private fun setupViews() {
        binding.rvTrack.layoutManager = LinearLayoutManager(this)
        binding.rvTrack.adapter = searchAdapter
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = historyAdapter

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }

        binding.backSearch.setNavigationOnClickListener {
            finish()
        }

        binding.errorView.setOnClickListener {
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
        viewModel.searchState.observe(this) { state ->
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

    private fun onTrackClick(track: com.example.playlistmaker.search.domain.model.Track) {
        viewModel.addTrackToHistory(track)
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_TRACK, track)
        }
        startActivity(intent)
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

    private fun showResults(tracks: List<com.example.playlistmaker.search.domain.model.Track>) {
        binding.historyContainer.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.rvTrack.visibility = View.VISIBLE
        searchAdapter.updateData(tracks)
    }

    private fun showSearchHistory(tracks: List<com.example.playlistmaker.search.domain.model.Track>) {
        binding.rvTrack.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.VISIBLE
        historyAdapter.updateData(tracks)
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
}