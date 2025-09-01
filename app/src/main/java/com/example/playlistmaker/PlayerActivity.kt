package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.MaterialToolbar

class PlayerActivity : AppCompatActivity() {

    private lateinit var currentTrack: Track
    private var isFavorite = false
    private var isPlaying = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        val track = intent.getParcelableExtra<Track>(EXTRA_TRACK)
        if (track == null) {
            finish()
            return
        }
        currentTrack = track

        val backButton = findViewById<MaterialToolbar>(R.id.back_player)

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

        setupPlayerUI(currentTrack)
    }

    private fun setupPlayerUI(track: Track) {

        val artworkImageView = findViewById<ImageView>(R.id.ivArtworkLarge)
        val trackNameTextView = findViewById<TextView>(R.id.tvTrackNamePlayer)
        val artistNameTextView = findViewById<TextView>(R.id.tvArtistNamePlayer)
        val trackTimeTextView = findViewById<TextView>(R.id.tvTrackTimePlayer)
        val playButton = findViewById<ImageButton>(R.id.ivPlayButton)
        val addToPlaylistButton = findViewById<ImageButton>(R.id.ivAddToPlaylist)
        val favoriteButton = findViewById<ImageButton>(R.id.ivAddToFavorites)

        val durationValueTextView = findViewById<TextView>(R.id.tvDurationValue)
        val albumNameTextView = findViewById<TextView>(R.id.tvAlbumName)
        val releaseYearTextView = findViewById<TextView>(R.id.tvReleaseYear)
        val genreTextView = findViewById<TextView>(R.id.tvGenre)
        val countryTextView = findViewById<TextView>(R.id.tvCountry)

        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        trackTimeTextView.text = track.getFormattedTime()
        durationValueTextView.text = track.getFormattedTime()

        val cornerRadiusInPx = (16 * resources.displayMetrics.density).toInt()
        val options = RequestOptions()
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .transform(RoundedCorners(cornerRadiusInPx))


        val artworkUrl = track.getHighResArtworkUrl()
        if (!artworkUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(artworkUrl)
                .apply(options)
                .into(artworkImageView)
        } else {
            if (!track.artworkUrl100.isNullOrEmpty()) {
                Glide.with(this)
                    .load(track.artworkUrl100)
                    .apply(options)
                    .into(artworkImageView)
            } else {
                Glide.with(this)
                    .load(R.drawable.ic_placeholder_45)
                    .apply(options)
                    .into(artworkImageView)
            }
        }

        setupOptionalField(
            track.collectionName,
            albumNameTextView,
            findViewById(R.id.tvAlbumLabel),
            findViewById(R.id.playerTrackAlbumGroup)
        )
        setupOptionalField(
            track.getReleaseYear(),
            releaseYearTextView,
            findViewById(R.id.tvReleaseYearLabel),
            findViewById(R.id.playerTrackYearGroup)
        )
        setupOptionalField(
            track.primaryGenreName,
            genreTextView,
            findViewById(R.id.tvGenreLabel),
            null
        )
        setupOptionalField(
            track.country,
            countryTextView,
            findViewById(R.id.tvCountryLabel),
            null
        )


        playButton.setOnClickListener {
            if (isPlaying) {
                pausePlayback()
                playButton.setImageResource(R.drawable.ic_play_arrow)
                playButton.tag = TAG_PAUSED
            } else {
                startPlayback()
                playButton.setImageResource(R.drawable.ic_pause)
                playButton.tag = TAG_PLAYING
            }
            isPlaying = !isPlaying
        }



        addToPlaylistButton.setOnClickListener {
            //добавление в плейлист
        }

        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteButton(favoriteButton)
            //Сохранить состояние избранного
        }

        updateFavoriteButton(favoriteButton)
    }

    private fun setupOptionalField(
        value: String?,
        valueTextView: TextView,
        labelTextView: TextView,
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

    private fun updateFavoriteButton(favoriteButton: ImageButton) {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled_51)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border_51)
        }
    }
    private fun startPlayback() {
        //Реализовать начало воспроизведения
    }

    private fun pausePlayback() {
        //Реализовать паузу воспроизведения
    }

    override fun onPause() {
        super.onPause()
        pausePlayback()

        if (isFinishing) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_PLAYING, isPlaying)
        outState.putBoolean(KEY_IS_FAVORITE, isFavorite)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isPlaying = savedInstanceState.getBoolean(KEY_IS_PLAYING, false)
        isFavorite = savedInstanceState.getBoolean(KEY_IS_FAVORITE, false)

        updatePlayButtonState()
        updateFavoriteButton(findViewById(R.id.ivAddToFavorites))
    }

    private fun updatePlayButtonState() {
        val playButton = findViewById<ImageButton>(R.id.ivPlayButton)
        if (isPlaying) {
            playButton.setImageResource(R.drawable.ic_pause)
            playButton.tag = TAG_PLAYING
            startPlayback()
        } else {
            playButton.setImageResource(R.drawable.ic_play_arrow)
            playButton.tag = TAG_PAUSED
        }
    }

    companion object {
        const val EXTRA_TRACK = "track"
        const val TAG_PLAYING = "playing"
        const val TAG_PAUSED = "paused"

        private const val KEY_IS_PLAYING = "is_playing"
        private const val KEY_IS_FAVORITE = "is_favorite"
    }

}