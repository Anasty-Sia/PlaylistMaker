package com.example.playlistmaker.library.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistDetailsBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.adapters.MenuBottomSheetAdapter
import com.example.playlistmaker.library.ui.adapters.PlaylistImageLoader
import com.example.playlistmaker.library.ui.adapters.PlaylistTracksAdapter
import com.example.playlistmaker.library.ui.view_model.PlaylistDetailsViewModel
import com.example.playlistmaker.root.RootActivity
import com.example.playlistmaker.search.domain.model.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistDetailsFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistDetailsViewModel by viewModel()
    private val args: PlaylistDetailsFragmentArgs by navArgs()

    private lateinit var tracksAdapter: PlaylistTracksAdapter
    private lateinit var tracksBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var backPressedCallback: OnBackPressedCallback


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? RootActivity)?.hideBottomNavigationView()

        setupToolbar()
        setupEdgeToEdgeInsets()
        setupBottomSheets()
        setupRecyclerView()
        setupMenuRecyclerView()
        setupListeners()
        observeViewModel()
        setupBackNavigation()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }
    }

    private fun setupBackNavigation() {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (menuBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    hideMenuBottomSheet()
                } else {
                    findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )
    }

    private fun setupBottomSheets() {
        tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer)
        tracksBottomSheetBehavior.isHideable = false
        tracksBottomSheetBehavior.peekHeight = 500
        tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED


        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.menuBottomSheetContainer)
        menuBottomSheetBehavior.isHideable = true
        menuBottomSheetBehavior.peekHeight = 0
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        menuBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                if (_binding == null) return

                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.isVisible = false
                        binding.overlay.alpha = 0f
                        binding.root.post {
                            try {
                                if (_binding != null && ::tracksBottomSheetBehavior.isInitialized) {
                                    tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.overlay.isVisible = true
                        binding.overlay.alpha = 0.6f
                    }
                    else -> {
                        binding.overlay.isVisible = true
                        binding.overlay.alpha = 0.6f
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (_binding == null) return
                binding.overlay.alpha = slideOffset * 0.6f
            }
        })

        binding.overlay.setOnClickListener {
            hideMenuBottomSheet()
        }
    }

    private fun setupRecyclerView() {
        tracksAdapter = PlaylistTracksAdapter(
            onTrackClick = { track ->
                navigateToPlayer(track)
            },
            onTrackLongClick = { track ->
                showDeleteTrackDialog(track)
            }
        )

        binding.rvTracks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTracks.adapter = tracksAdapter
    }

    private fun setupMenuRecyclerView() {
        val menuAdapter = MenuBottomSheetAdapter { menuItemType ->
            when (menuItemType) {
                MenuBottomSheetAdapter.MenuItemType.SHARE -> {
                    hideMenuBottomSheet()
                    sharePlaylist()
                }
                MenuBottomSheetAdapter.MenuItemType.EDIT -> {
                    hideMenuBottomSheet()
                    navigateToEditPlaylist()
                }
                MenuBottomSheetAdapter.MenuItemType.DELETE -> {
                    hideMenuBottomSheet()
                    showDeletePlaylistDialog()
                }
            }
        }
        binding.rvMenuBottomSheet.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMenuBottomSheet.adapter = menuAdapter
    }

    private fun setupListeners() {
        binding.btnShare.setOnClickListener {
            sharePlaylist()
        }

        binding.btnMenu.setOnClickListener {
            showMenuBottomSheet()
        }
    }

    private fun observeViewModel() {
        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            if (_binding == null) return@observe
            if (playlist == null) {
                Toast.makeText(requireContext(), "Плейлист не найден", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                return@observe
            }
            updatePlaylistInfo(playlist)
        }

        viewModel.playlistTracks.observe(viewLifecycleOwner) { tracks ->
            if (_binding == null) return@observe
            updateTracksList(tracks)
        }

        viewModel.totalDuration.observe(viewLifecycleOwner) { duration ->
            if (_binding == null) return@observe
            binding.tvTotalDuration.text = duration
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (_binding == null) return@observe
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.bottomSheetProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.shareResult.observe(viewLifecycleOwner) { shareText ->
            if (_binding == null) return@observe
            shareText?.let {
                sharePlaylistText(it)
            }
        }

        viewModel.showEmptyPlaylistToast.observe(viewLifecycleOwner) { show ->
            if (_binding == null) return@observe
            if (show) {
                Toast.makeText(
                    requireContext(),
                    "В этом плейлисте нет списка треков, которым можно поделиться",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetShowEmptyPlaylistToast()
            }
        }

        viewModel.playlistDeleted.observe(viewLifecycleOwner) { isDeleted ->
            if (_binding == null) return@observe
            if (isDeleted) {
                (activity as? RootActivity)?.showGlobalToast("Плейлист удалён")
                findNavController().navigateUp()
            }
        }
    }

    private fun updatePlaylistInfo(playlist: Playlist) {
        PlaylistImageLoader.loadPlaylistCover(binding.ivCover, playlist.coverImagePath)

        binding.tvPlaylistName.text = playlist.name
        binding.tvDescription.text = playlist.description ?: ""
        binding.tvDescription.isVisible = !playlist.description.isNullOrEmpty()

        val trackCountText = requireContext().resources.getQuantityString(
            R.plurals.tracks_count,
            playlist.trackCount,
            playlist.trackCount
        )
        binding.tvTrackCount.text = trackCountText

        with(binding) {
            PlaylistImageLoader.loadPlaylistCover(ivMenuPlaylistCover, playlist.coverImagePath, cornerRadiusDp = 8)

            tvMenuPlaylistName.text = playlist.name

            val trackCountText = resources.getQuantityString(
                R.plurals.tracks_count,
                playlist.trackCount,
                playlist.trackCount
            )
            tvMenuPlaylistTrackCount.text = trackCountText

            playlistInfoContainer.setOnClickListener {
                hideMenuBottomSheet()
            }
        }
    }

    private fun updateTracksList(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            binding.emptyTracksState.visibility = View.VISIBLE
            binding.rvTracks.visibility = View.GONE
        } else {
            binding.emptyTracksState.visibility = View.GONE
            binding.rvTracks.visibility = View.VISIBLE
            tracksAdapter.updateData(tracks)

            binding.root.post {
                try {
                    if (_binding != null && ::tracksBottomSheetBehavior.isInitialized) {
                        tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sharePlaylist() {
        viewModel.sharePlaylist()
    }

    private fun sharePlaylistText(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_playlist_via)))
    }

    private fun navigateToEditPlaylist() {
        val currentPlaylist = viewModel.playlist.value ?: return
        val bundle = bundleOf("playlist" to currentPlaylist)
        findNavController().navigate(R.id.editPlaylistFragment, bundle)
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_track_title))
            .setMessage(getString(R.string.delete_track_message))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                deleteTrack(track)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteTrack(track: Track) {
        viewModel.deleteTrackFromPlaylist(track.trackId)
        Toast.makeText(
            requireContext(),
            getString(R.string.track_deleted),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showDeletePlaylistDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_playlist_title))
            .setMessage(getString(R.string.delete_playlist_message))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                viewModel.deletePlaylist()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showMenuBottomSheet() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideMenuBottomSheet() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun navigateToPlayer(track: Track) {
        val bundle = Bundle().apply {
            putParcelable("track", track)
        }
        findNavController().navigate(
            R.id.playerFragment,
            bundle
        )
    }



    override fun onResume() {
        super.onResume()
        (activity as? RootActivity)?.hideBottomNavigationView()

        val playlistId = args.playlistId
        if (playlistId != 0L) {
            viewModel.loadPlaylist(playlistId)
            viewModel.loadPlaylistTracks(playlistId)
        } else {
            Toast.makeText(requireContext(), "Ошибка: ID плейлиста не указан", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? RootActivity)?.showBottomNavigationView()
        backPressedCallback.remove()
        _binding = null
    }




}