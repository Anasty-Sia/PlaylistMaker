package com.example.playlistmaker.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.Creator
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractorInterface
import com.example.playlistmaker.domain.interactor.SearchInteractorInterface
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchInteractor: SearchInteractorInterface
    private lateinit var searchHistoryInteractor: SearchHistoryInteractorInterface
    private val searchAdapter = TrackAdapter(emptyList()) { track -> onTrackClick(track) }
    private val historyAdapter = TrackAdapter(emptyList()) { track -> onTrackClick(track) }

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchInteractor = Creator.provideSearchInteractor(this)
        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(this)

        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupViews()
        setupSearchField()
        resetSearchState()
    }

    override fun onResume() {
        super.onResume()
        if (binding.inputSearch.text.isEmpty()) {
            showSearchHistory()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchRunnable?.let { handler.removeCallbacks(it) }
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
            lifecycleScope.launch {
                searchHistoryInteractor.clearSearchHistory()
                showSearchHistory()
            }
        }

        binding.backSearch.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupSearchField() {
        binding.inputSearch.doOnTextChanged { text, _, _, _ ->
            val searchText = text?.toString() ?: ""
                binding.clearIcon.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE

                searchRunnable?.let { handler.removeCallbacks(it) }
                if (searchText.isEmpty()) {
                    binding.progressBar.visibility = View.GONE
                    showSearchHistory()
                } else {
                    binding.historyContainer.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvTrack.visibility = View.GONE
                    binding.emptyResults.visibility = View.GONE
                    binding.errorView.visibility = View.GONE

                    searchRunnable = Runnable {
                        performSearch(searchText)
                    }

                    handler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_DELAY)
                }
            }

        binding.clearIcon.setOnClickListener {
            binding.inputSearch.text.clear()
            showSearchHistory()
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            binding.progressBar.visibility = View.GONE
            showSearchHistory()
            return
        }

        lifecycleScope.launch {
            binding.emptyResults.visibility = View.GONE
            binding.errorView.visibility = View.GONE
            binding.historyContainer.visibility = View.GONE

            try {
                val tracks = searchInteractor.searchTracks(query)
                if (tracks.isEmpty()) {
                    showEmptyState()
                } else {
                    showResults(tracks)
                }
            } catch (e: Exception) {
                showErrorState()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun onTrackClick(track: Track) {
        lifecycleScope.launch {
            searchHistoryInteractor.addTrackToHistory(track)
            val intent = Intent(this@SearchActivity, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_TRACK, track)
            }
            startActivity(intent)
        }
    }

    private fun showSearchHistory() {
        lifecycleScope.launch {
            val history = searchHistoryInteractor.getSearchHistory()
            if (history.isNotEmpty()) {
                binding.historyContainer.visibility = View.VISIBLE
                binding.rvTrack.visibility = View.GONE
                binding.emptyResults.visibility = View.GONE
                binding.errorView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                historyAdapter.updateData(history)
            } else {
                binding.historyContainer.visibility = View.GONE
                resetSearchState()
            }
        }
    }

    private fun showResults(tracks: List<Track>) {
        binding.historyContainer.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.rvTrack.visibility = View.VISIBLE
        searchAdapter.updateData(tracks)
    }

    private fun showEmptyState() {
        binding.historyContainer.visibility = View.GONE
        binding.rvTrack.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.emptyResults.visibility = View.VISIBLE
    }

    private fun showErrorState() {
        binding.historyContainer.visibility = View.GONE
        binding.rvTrack.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
    }

    private fun resetSearchState() {
        binding.historyContainer.visibility = View.GONE
        binding.rvTrack.visibility = View.GONE
        binding.emptyResults.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        searchAdapter.updateData(emptyList())

        if (binding.inputSearch.text.isEmpty()) {
            showSearchHistory()
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}