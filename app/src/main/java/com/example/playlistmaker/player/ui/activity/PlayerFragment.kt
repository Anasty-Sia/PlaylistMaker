package com.example.playlistmaker.player.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.adapters.PlaylistBottomSheetAdapter
import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.player.ui.view_model.AddToPlaylistStatus
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.root.RootActivity
import com.example.playlistmaker.search.domain.model.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val args: PlayerFragmentArgs by navArgs()
    private val viewModel: PlayerViewModel by viewModel()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetAdapter: PlaylistBottomSheetAdapter

    private var currentTrack: Track? = null

    private var isBottomSheetAnimating = false
    private var lastOpenTime = 0L
    private val MIN_OPEN_INTERVAL = 500L // мс
    private lateinit var backCallback: OnBackPressedCallback

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
        currentTrack = track
        setupPlayerUI(track)

        setupBottomSheet(track)

        viewModel.preparePlayer(track)
        observeViewModel()

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    hidePlaylistBottomSheet()
                } else {
                    viewModel.stopPlayback()
                    findNavController().popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
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

        binding.ivAddToPlaylist.setOnClickListener {
            showPlaylistBottomSheet()
        }

        binding.backPlayer.setNavigationOnClickListener {
            viewModel.stopPlayback()
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            if (!isAdded) return@observe

            updatePlayButtonState(state.playbackState is PlaybackState.PLAYING)
            updateFavoriteButton(state.isFavorite)
            binding.tvTrackTimePlayer.text = formatTime(state.currentPosition)

            state.addToPlaylistStatus?.let { status ->
                when (status) {
                    is AddToPlaylistStatus.Success -> {
                        showToast(getString(R.string.track_added_to_playlist, status.playlistName))
                    }
                    is AddToPlaylistStatus.AlreadyInPlaylist -> {
                        showToast(
                            getString(
                                R.string.track_already_in_playlist,
                                status.playlistName
                            )
                        )
                    }
                    is AddToPlaylistStatus.Error -> {
                        showToast(getString(R.string.error_adding_track))
                    }
                }
            }

            when (state.playbackState) {
                is PlaybackState.PREPARED -> {
                    binding.tvTrackTimePlayer.text = DEFAULT_TRACK_TIME
                }
                is PlaybackState.COMPLETED -> {
                    binding.tvTrackTimePlayer.text = DEFAULT_TRACK_TIME
                }
                is PlaybackState.ERROR -> {
                    binding.tvTrackTimePlayer.text = DEFAULT_TRACK_TIME
                    Toast.makeText(
                        requireContext(),
                        state.playbackState.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (!isAdded) return@observe

            Log.d("BottomSheet", "Playlists updated: ${playlists.size} items")

            bottomSheetAdapter.updateData(playlists)

            if (playlists.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvPlaylists.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.rvPlaylists.visibility = View.VISIBLE
            }
        }
    }

    private fun setupBottomSheet(track: Track) {

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
            isDraggable = true
            skipCollapsed = true
            isFitToContents = false

            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val expandedHeight = (screenHeight * 0.8).toInt()
            maxHeight = expandedHeight
            peekHeight = 0

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {

                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            isBottomSheetAnimating = false
                            binding.overlay.visibility = View.GONE
                            binding.bottomSheetContainer.visibility = View.GONE
                            viewModel.clearAddToPlaylistStatus()
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            isBottomSheetAnimating = false
                            binding.overlay.visibility = View.VISIBLE
                            binding.overlay.alpha = 1f
                            viewModel.loadPlaylists()
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {
                            isBottomSheetAnimating = true
                        }
                        else -> {
                            if (newState != BottomSheetBehavior.STATE_HIDDEN) {
                                binding.overlay.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (_binding == null) return
                    val alpha = when {
                        slideOffset <= 0 -> 0f
                        slideOffset >= 1 -> 1f
                        else -> slideOffset
                    }
                    binding.overlay.alpha = alpha
                }
            })
        }

        bottomSheetAdapter = PlaylistBottomSheetAdapter(
            onPlaylistClick = { playlist ->
                addTrackToPlaylist(playlist, track)
            },
            currentTrackId = track.trackId
        )

        binding.rvPlaylists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaylists.adapter = bottomSheetAdapter

        binding.createPlaylistButton.setOnClickListener {
            navigateToNewPlaylist()
        }

        binding.overlay.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED &&
                !isBottomSheetAnimating) {
                hidePlaylistBottomSheet()
            }
        }

        binding.mainContent.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                hidePlaylistBottomSheet()
            }
        }

    }

    private fun showPlaylistBottomSheet() {
        val now = System.currentTimeMillis()
        if (now - lastOpenTime < MIN_OPEN_INTERVAL) {
            return
        }
        lastOpenTime = now

        isBottomSheetAnimating = false

        if (isBottomSheetAnimating) {
            return
        }

        try {
            isBottomSheetAnimating = true

            binding.overlay.visibility = View.VISIBLE
            binding.overlay.alpha = 0f

            binding.bottomSheetContainer.visibility = View.VISIBLE
            binding.bottomSheetContainer.translationY = binding.bottomSheetContainer.height.toFloat()

            binding.overlay.animate()
                .alpha(1f)
                .setDuration(200)
                .start()

            binding.bottomSheetContainer.animate()
                .translationY(0f)
                .setDuration(300)
                .withEndAction {
                    isBottomSheetAnimating = false
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    viewModel.loadPlaylists()
                }
                .start()

        } catch (e: Exception) {
            isBottomSheetAnimating = false
        }
    }
    private fun hidePlaylistBottomSheet() {
        if (isBottomSheetAnimating) {
            return
        }

        if (_binding == null || binding.bottomSheetContainer.visibility == View.GONE) {
            return
        }

        try {
            isBottomSheetAnimating = true

            binding.bottomSheetContainer.animate()
                .translationY(binding.bottomSheetContainer.height.toFloat())
                .setDuration(250)
                .withEndAction {
                    if (_binding != null && isAdded && view != null) {
                        binding.bottomSheetContainer.visibility = View.GONE
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        isBottomSheetAnimating = false
                    }
                    isBottomSheetAnimating = false
                }
                .start()

            binding.overlay.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    if (_binding != null && isAdded && view != null) {
                        binding.overlay.visibility = View.GONE
                    }
                }
                .start()
        } catch (e: Exception) {
            isBottomSheetAnimating = false
        }
    }


    private fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        if (!isAdded || view == null) return

        lifecycleScope.launch {
            try {
                viewModel.addTrackToPlaylist(playlist.playlistId, track)
                delay(1000)

                val currentState = viewModel.playerState.value
                currentState?.addToPlaylistStatus?.let { status ->
                    when (status) {
                        is AddToPlaylistStatus.Success -> {
                            if (isAdded && view != null) {
                                hidePlaylistBottomSheet()
                            }
                        }
                        is AddToPlaylistStatus.AlreadyInPlaylist -> {
                        }
                        is AddToPlaylistStatus.Error -> {
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PlayerFragment", "Error adding track to playlist", e)
            }
        }

    }
    private fun navigateToNewPlaylist() {
        if (!isAdded || view == null) return

        hidePlaylistBottomSheet()

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded) return@postDelayed


            try {
                val bundle = if (currentTrack != null) {
                    Bundle().apply {
                    putParcelable("track_to_add", currentTrack)
                }
            }  else{
                null
            }

                if (bundle != null) {
                    findNavController().navigate(R.id.action_playerFragment_to_newPlaylistFragment, bundle)
                } else {
                    findNavController().navigate(R.id.action_playerFragment_to_newPlaylistFragment)
                }
            } catch (e: Exception) {
                Log.e("PlayerFragment", "Navigation error to new playlist", e)
            }
        }, 300)
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

    private fun showToast(message: String) {
        if (!isAdded) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            show()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopPlayback()

        if (::bottomSheetBehavior.isInitialized &&
            bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            hidePlaylistBottomSheet()
        }
    }

    override fun onResume() {
        super.onResume()
        isBottomSheetAnimating = false
        viewModel.loadPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? RootActivity)?.showBottomNavigationView()

        if (::backCallback.isInitialized) {
            backCallback.remove()
        }

        _binding = null
    }

    private companion object {
        const val DEFAULT_TRACK_TIME = "00:00"
    }
}