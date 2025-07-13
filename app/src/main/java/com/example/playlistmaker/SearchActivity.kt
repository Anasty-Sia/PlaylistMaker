package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {

    private var currentSearchText: String = ""
    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var rvTrack: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    private val sampleTracks = listOf(
        Track("Smells Like Teen Spirit", "Nirvana", "5:01", "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"),
        Track("Billie Jean", "Michael Jackson", "4:35", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"),
        Track("Stayin' Alive", "Bee Gees", "4:10", "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"),
        Track("Whole Lotta Love", "Led Zeppelin", "5:33", "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"),
        Track("Sweet Child O'Mine", "Guns N' Roses", "5:03", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputSearch)
        clearButton = findViewById(R.id.clearIcon)
        val backButton = findViewById<MaterialToolbar>(R.id.back_search)
        rvTrack = findViewById(R.id.rvTrack)

        rvTrack.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        trackAdapter = TrackAdapter(emptyList())
        rvTrack.adapter = trackAdapter


        if (savedInstanceState != null) {
            currentSearchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
            if (currentSearchText.isNotEmpty()) {
                inputEditText.setText(currentSearchText)
                clearButton.visibility = View.VISIBLE
                performSearch(currentSearchText)
            }
        }

        setupBackButton(backButton)
        setupSearchField()
        setupClearButton()

    }

    private fun performSearch(query: String) {

        if (query.isEmpty()) {
            trackAdapter.updateData(emptyList())
            return
        }

        val normalizedQuery = query.trim().lowercase()

        val filteredTracks = sampleTracks.filter { track ->
            track.trackName.lowercase().startsWith(normalizedQuery) ||
                    track.artistName.lowercase().startsWith(normalizedQuery)
        }

        val sortedTracks = filteredTracks.sortedWith(compareBy(
            { it.trackName.lowercase() },
            { it.artistName.lowercase() }
        ))

        trackAdapter.updateData(sortedTracks)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, currentSearchText)
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
            finish()
        }
    }

    private fun setupSearchField() {
        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputEditText.text.isNotEmpty()) {
                showKeyboard(inputEditText)
            }
        }

        inputEditText.post {
            if (currentSearchText.isNotEmpty()) {
                inputEditText.requestFocus()
                showKeyboard(inputEditText)
            }
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s?.toString() ?: ""
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                performSearch(currentSearchText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            inputEditText.text.clear()
            hideKeyboard(inputEditText)
            inputEditText.clearFocus()
            currentSearchText = ""
            clearButton.visibility = View.GONE
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

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
    }

}
