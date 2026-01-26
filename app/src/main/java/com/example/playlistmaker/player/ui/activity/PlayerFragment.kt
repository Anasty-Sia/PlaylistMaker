package com.example.playlistmaker.player.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.root.RootActivity
import com.example.playlistmaker.search.domain.model.Track
import org.koin.androidx.viewmodel.ext.android.viewModel


class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val args: PlayerFragmentArgs by navArgs()
    private val viewModel: PlayerViewModel by viewModel()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? RootActivity)?.hideBottomNavigationView()
        setupEdgeToEdgeInsets()


        val track = args.track
        setupPlayerUI(track)

        viewModel.preparePlayer(track)
        observeViewModel()
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.backPlayer) { view, insets ->
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
        binding.ivAddToFavorites.setOnClickListener {
            viewModel.toggleFavorite()
        }

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

        binding.backPlayer.setNavigationOnClickListener {
            viewModel.stopPlayback()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
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
                    Toast.makeText(requireContext(), state.playbackState.message, Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? RootActivity)?.showBottomNavigationView()
        _binding = null
    }

    private companion object {
        const val DEFAULT_TRACK_TIME = "00:00"
    }
}