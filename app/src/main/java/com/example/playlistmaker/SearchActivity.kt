package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.converter.gson.GsonConverterFactory



class SearchActivity : AppCompatActivity() {

    private val iTunesBaseUrl = "https://itunes.apple.com"
    private lateinit var iTunesService: iTunesSearchAPI
    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var searchHistory: SearchHistory
    private lateinit var rvTrack: RecyclerView
    private lateinit var rvHistory: RecyclerView
    private lateinit var emptyResultsView: View
    private lateinit var errorView: View
    private lateinit var refreshButton: View
    private lateinit var historyContainer: View

    private lateinit var progressBar: View

    private lateinit var clearHistoryButton: View
    private lateinit var historyTitle: TextView

    private var currentSearchText: String = ""
    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView

    // Debounce для кликов
    private var isClickAllowed = true
    private val clickHandler = Handler(Looper.getMainLooper())

    // Debounce для поиска
    private val searchHandler = Handler(Looper.getMainLooper())
    private lateinit var searchRunnable: Runnable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputSearch)
        clearButton = findViewById(R.id.clearIcon)
        val backButton = findViewById<MaterialToolbar>(R.id.back_search)
        rvTrack = findViewById(R.id.rvTrack)
        rvHistory = findViewById(R.id.rvHistory)
        emptyResultsView = findViewById(R.id.empty_results)
        errorView = findViewById(R.id.error_view)
        refreshButton = findViewById(R.id.refresh_button)
        historyContainer = findViewById(R.id.history_container)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        historyTitle = findViewById(R.id.history_title)
        progressBar = findViewById(R.id.progressBar)

        searchHistory= SearchHistory(this)
        setupRetrofit()
        setupSearchRunnable()
        setupViews()
        setupBackButton(backButton)
        setupSearchField()
        setupClearButton()
        setupHistory()

        if (savedInstanceState != null) {
            currentSearchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
            if (currentSearchText.isNotEmpty()) {
                inputEditText.setText(currentSearchText)
                clearButton.visibility = View.VISIBLE
            }
        }
    }

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(iTunesBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        iTunesService = retrofit.create(iTunesSearchAPI::class.java)

    }

    private fun setupSearchRunnable() {
        searchRunnable = Runnable {
            if (currentSearchText.isNotEmpty()) {
                performSearch(currentSearchText)
            }
        }
    }

    private fun setupViews() {

        searchAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                addToSearchHistory(track)
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("track", track)
                }
                startActivity(intent)
            }
        }

        historyAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                addToSearchHistory(track)
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("track", track)
                }
                startActivity(intent)
            }
        }

        rvTrack.layoutManager = LinearLayoutManager(this)
        rvTrack.adapter = searchAdapter
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter

        refreshButton.setOnClickListener {
            if (clickDebounce()) {
                if (currentSearchText.isNotEmpty()) {
                    refreshButton.isEnabled = false
                    performSearch(currentSearchText)
                    refreshButton.postDelayed({ refreshButton.isEnabled = true }, 1000)
                } else {
                    inputEditText.requestFocus()
                    showKeyboard(inputEditText)
                }
            }
        }

        clearHistoryButton.setOnClickListener {
            if (clickDebounce()) {
                searchHistory.clearHistory()
                showSearchHistory()
            }
        }

    }

    // Функция debounce для кликов
    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            clickHandler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    // Функция debounce для поиска
    private fun searchDebounce() {
        searchHandler.removeCallbacks(searchRunnable)
        searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun addToSearchHistory(track: Track) {
        searchHistory.addTrack(track)
        showSearchHistory()
    }

    private fun setupHistory() {
        showSearchHistory()
    }

    private fun showSearchHistory() {
        val history = searchHistory.getHistory()
        if (history.isNotEmpty() && currentSearchText.isEmpty() && inputEditText.hasFocus()) {
            historyContainer.visibility = View.VISIBLE
            historyAdapter.updateData(history)
        } else {
            historyContainer.visibility = View.GONE
        }
    }

    private fun performSearch(searchText: String) {
        if (searchText.isBlank()) {
            resetSearchState()
            showSearchHistory()
            return
        }

        historyContainer.visibility = View.GONE
        emptyResultsView.visibility = View.GONE
        errorView.visibility = View.GONE
        rvTrack.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        iTunesService.search(searchText).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val tracks = response.body()?.results ?: emptyList()
                    if (tracks.isEmpty()) {
                        showEmptyState(R.string.no_results, R.drawable.ic_no_results_120)
                    } else {
                        showResults(tracks)
                    }
                } else {
                    showErrorState(true)
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showErrorState(true)

            }
        })
    }


    private fun showEmptyState(messageRes: Int, iconRes: Int) {
        emptyResultsView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        rvTrack.visibility = View.GONE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        emptyResultsView.findViewById<TextView>(R.id.emptyResultsText).text = getString(messageRes)
        emptyResultsView.findViewById<ImageView>(R.id.emptyResultsIcon).setImageResource(iconRes)
    }


    private fun showResults(tracks: List<Track>) {
        emptyResultsView.visibility = View.GONE
        errorView.visibility = View.GONE
        rvTrack.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        searchAdapter.updateData(tracks)
    }

    private fun showErrorState(show: Boolean) {
        if (show) {
            errorView.visibility = View.VISIBLE
            emptyResultsView.visibility = View.GONE
            rvTrack.visibility = View.GONE
            historyContainer.visibility = View.GONE
            progressBar.visibility = View.GONE
        } else {
            errorView.visibility = View.GONE
        }
    }

    private fun resetSearchState() {
        emptyResultsView.visibility = View.GONE
        errorView.visibility = View.GONE
        rvTrack.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        searchAdapter.updateData(emptyList())
    }

    private fun setupBackButton(backButton: MaterialToolbar) {
        ViewCompat.setOnApplyWindowInsetsListener(backButton) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }
        backButton.setNavigationOnClickListener {
            if (clickDebounce()) {
                finish()
            }
        }
    }

    private fun setupSearchField() {
        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (inputEditText.text.isEmpty()) {
                    showSearchHistory()
                }
                showKeyboard(inputEditText)
            }
        }

        inputEditText.post {
            if (currentSearchText.isNotEmpty()) {
                inputEditText.requestFocus()
                showKeyboard(inputEditText)
            }
            else {
                showSearchHistory()
            }
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s?.toString() ?: ""
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                if (s.isNullOrEmpty()) {
                    showSearchHistory()
                } else {
                    historyContainer.visibility = View.GONE
                    searchDebounce()
                }

            }
            override fun afterTextChanged(s: Editable?) {}
        })

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchHandler.removeCallbacks(searchRunnable)
                performSearch(currentSearchText)
                hideKeyboard(inputEditText)
                true
            } else {
                false
            }
        }
    }

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            inputEditText.text.clear()
            hideKeyboard(inputEditText)
            inputEditText.clearFocus()
            currentSearchText = ""
            clearButton.visibility = View.GONE
            showErrorState(false)
            resetSearchState()
            showSearchHistory()
            searchHandler.removeCallbacks(searchRunnable)
        }
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, currentSearchText)
    }

    override fun onDestroy() {
        super.onDestroy()
        clickHandler.removeCallbacksAndMessages(null)
        searchHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
