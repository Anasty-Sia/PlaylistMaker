package com.example.playlistmaker.presentation

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.playlistmaker.domain.model.Track
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Locale


class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var currentTrack: Track

    private var isFavorite = false
    private var isPlaying = false
    private var mediaPlayer: MediaPlayer? = null
    private var playbackPosition = 0

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var progressRunnable: Runnable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentTrack = intent.getParcelableExtra(EXTRA_TRACK) ?: run {
            finish()
            return
        }

        setupSystemBars()
        setupPlayerUI(currentTrack)
        setupBackButton()
        initializeMediaPlayer()
        setupProgressRunnable()
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
        binding.tvTrackTimePlayer.text = DEFAULT_TRACK_TIME
        binding.tvDurationValue.text = track.getFormattedTime()

        val cornerRadiusInPx = (8 * resources.displayMetrics.density).toInt()
        val artworkUrl = track.getHighResArtworkUrl()?: track.artworkUrl100
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
            if (isPlaying) {
                pausePlayback()
            } else {
                startPlayback()
            }
        }

        binding.ivAddToPlaylist.setOnClickListener {
            // добавление в плейлист
        }

        binding.ivAddToFavorites.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteButton()
            // Сохранить состояние избранного
        }

        updateFavoriteButton()
    }

    private fun setupBackButton() {
        binding.backPlayer.setNavigationOnClickListener {
            stopPlayback()
            finish()
        }
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                onPlaybackCompleted()
            }
        }
    }

    private fun setupProgressRunnable() {
        progressRunnable = object : Runnable {
            override fun run() {
                updateProgress()
                if (isPlaying && mediaPlayer?.isPlaying == true) {
                    handler.postDelayed(this, PROGRESS_UPDATE_DELAY)
                }
            }
        }
    }

    private fun updateProgress() {
        mediaPlayer?.let { player ->
            val currentPosition = player.currentPosition
            binding.tvTrackTimePlayer.text = formatTime(currentPosition)
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        return dateFormat.format(milliseconds)
    }

    private fun startPlayback() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    return
                }

                if (playbackPosition > 0 && mediaPlayer != null) {
                    player.seekTo(playbackPosition)
                    player.start()
                    isPlaying = true
                    updatePlayButtonState()
                    handler.post(progressRunnable)
                } else {
                    if (currentTrack.previewUrl.isNullOrEmpty()) {
                        return
                    }

                    player.reset()
                    player.setDataSource(currentTrack.previewUrl)
                    player.prepareAsync()

                    player.setOnPreparedListener {
                        it.start()
                        isPlaying = true
                        playbackPosition = 0
                        updatePlayButtonState()
                        handler.post(progressRunnable)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                playbackPosition = player.currentPosition
            }
        }
        isPlaying = false
        handler.removeCallbacks(progressRunnable)
        updatePlayButtonState()
    }

    private fun onPlaybackCompleted() {
        isPlaying = false
        playbackPosition = 0

        handler.removeCallbacks(progressRunnable)
        binding.tvTrackTimePlayer.text = DEFAULT_TRACK_TIME
        updatePlayButtonState()
        mediaPlayer?.reset()
    }

    private fun stopPlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
        }
        isPlaying = false
        playbackPosition = 0
        handler.removeCallbacks(progressRunnable)
        binding.tvTrackTimePlayer.text = DEFAULT_TRACK_TIME
        updatePlayButtonState()
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

    private fun updateFavoriteButton() {
        if (isFavorite) {
            binding.ivAddToFavorites.setImageResource(R.drawable.ic_favorite_filled_51)
        } else {
            binding.ivAddToFavorites.setImageResource(R.drawable.ic_favorite_border_51)
        }
    }

    private fun updatePlayButtonState() {
        if (isPlaying) {
            binding.ivPlayButton.setImageResource(R.drawable.ic_pause)
            binding.ivPlayButton.tag = TAG_PLAYING
        } else {
            binding.ivPlayButton.setImageResource(R.drawable.ic_play_arrow)
            binding.ivPlayButton.tag = TAG_PAUSED
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_PLAYING, isPlaying)
        outState.putBoolean(KEY_IS_FAVORITE, isFavorite)
        outState.putInt(KEY_PLAYBACK_POSITION, playbackPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isPlaying = savedInstanceState.getBoolean(KEY_IS_PLAYING, false)
        isFavorite = savedInstanceState.getBoolean(KEY_IS_FAVORITE, false)
        playbackPosition = savedInstanceState.getInt(KEY_PLAYBACK_POSITION, 0)

        updatePlayButtonState()
        updateFavoriteButton()

        if (playbackPosition > 0) {
            binding.tvTrackTimePlayer.text = formatTime(playbackPosition)
        }
    }

    companion object {
        const val EXTRA_TRACK = "track"
        const val TAG_PLAYING = "playing"
        const val TAG_PAUSED = "paused"
        const val DEFAULT_TRACK_TIME = "00:00"

        private const val KEY_IS_PLAYING = "is_playing"
        private const val KEY_IS_FAVORITE = "is_favorite"
        private const val KEY_PLAYBACK_POSITION = "playback_position"
        private const val PROGRESS_UPDATE_DELAY = 300L
    }
}