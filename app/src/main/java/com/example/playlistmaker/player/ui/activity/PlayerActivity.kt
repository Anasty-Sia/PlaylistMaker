package com.example.playlistmaker.player.ui.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.domain.model.Track
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private val viewModel: PlayerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val track = intent.getParcelableExtra<Track>(EXTRA_TRACK) ?: run {
            finish()
            return
        }

        setupSystemBars()
        setupPlayerUI(track)
        setupBackButton()


        viewModel.preparePlayer(track)
        observeViewModel()
    }

    private fun setupSystemBars() {
        val backButton = findViewById<MaterialToolbar>(R.id.back_player)

        ViewCompat.setOnApplyWindowInsetsListener(backButton) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }
    }

    private fun setupPlayerUI(track: Track) {
        binding.tvTrackNamePlayer.text = track.trackName
        binding.tvArtistNamePlayer.text = track.artistName
        binding.tvDurationValue.text = track.getFormattedTime()

        val cornerRadiusInPx = (8 * resources.displayMetrics.density).toInt()
        val artworkUrl = track.getHighResArtworkUrl() ?: track.artworkUrl100
        Glide.with(this)
            .load(artworkUrl)
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .transform(RoundedCorners(cornerRadiusInPx))
            .into(binding.ivArtworkLarge)

        setupOptionalField(
            track.collectionName,
            binding.tvAlbumName,
            binding.tvAlbumLabel,
            binding.playerTrackAlbumGroup
        )
        setupOptionalField(
            track.getReleaseYear(),
            binding.tvReleaseYear,
            binding.tvReleaseYearLabel,
            binding.playerTrackYearGroup
        )
        setupOptionalField(
            track.primaryGenreName,
            binding.tvGenre,
            binding.tvGenreLabel,
            null
        )
        setupOptionalField(
            track.country,
            binding.tvCountry,
            binding.tvCountryLabel,
            null
        )

        binding.ivPlayButton.setOnClickListener {
            viewModel.playPause()
        }

        binding.ivAddToFavorites.setOnClickListener {
            viewModel.toggleFavorite()
        }
    }

    private fun setupBackButton() {
        binding.backPlayer.setNavigationOnClickListener {
            viewModel.stopPlayback()
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.playerState.observe(this) { state ->
            updatePlayButtonState(state.playbackState is PlaybackState.PLAYING)
            updateFavoriteButton(state.isFavorite)
            binding.tvTrackTimePlayer.text = formatTime(state.currentPosition)

            when (state.playbackState) {
                is PlaybackState.PREPARED -> {
                    binding.tvTrackTimePlayer.text = DEFAULT_TRACK_TIME
                }
                is PlaybackState.COMPLETED -> {
                    binding.tvTrackTimePlayer.text = DEFAULT_TRACK_TIME
                }
                is PlaybackState.ERROR -> {

                    binding.tvTrackTimePlayer.text = DEFAULT_TRACK_TIME
                }
                else -> {}
            }
        }
    }

    private fun updatePlayButtonState(isPlaying: Boolean) {
        if (isPlaying) {
            binding.ivPlayButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.ivPlayButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            binding.ivAddToFavorites.setImageResource(R.drawable.ic_favorite_filled_51)
        } else {
            binding.ivAddToFavorites.setImageResource(R.drawable.ic_favorite_border_51)
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun setupOptionalField(
        value: String?,
        valueTextView: android.widget.TextView,
        labelTextView: android.widget.TextView,
        group: View?
    ) {
        if (!value.isNullOrEmpty()) {
            valueTextView.text = value
            valueTextView.visibility = View.VISIBLE
            labelTextView.visibility = View.VISIBLE
            group?.visibility = View.VISIBLE
        } else {
            valueTextView.visibility = View.GONE
            labelTextView.visibility = View.GONE
            group?.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopPlayback()
    }

    companion object {
        const val EXTRA_TRACK = "track"
        const val DEFAULT_TRACK_TIME = "00:00"
    }
}