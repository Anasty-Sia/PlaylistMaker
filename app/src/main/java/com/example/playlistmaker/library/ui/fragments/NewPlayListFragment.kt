package com.example.playlistmaker.library.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlayListBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.view_model.PlaylistsViewModel
import com.example.playlistmaker.root.RootActivity
import com.example.playlistmaker.search.domain.model.Track
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlayListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var savedCoverImagePath: String? = null
    private var trackToAdd: Track? = null

    private var isCreating = false
    private lateinit var backPressedCallback: OnBackPressedCallback

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri

                binding.ivAddPhotoIcon.visibility = View.GONE
                binding.ivCover.visibility = View.VISIBLE

                Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.ic_placeholder_45)
                    .error(R.drawable.ic_placeholder_45)
                    .centerCrop()
                    .into(binding.ivCover)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.permission_required_for_image),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            trackToAdd = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable("track_to_add", Track::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable("track_to_add")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let { bundle ->
            selectedImageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("SELECTED_IMAGE_URI", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable("SELECTED_IMAGE_URI")
            }
        }

            selectedImageUri?.let { uri ->
                binding.ivAddPhotoIcon.visibility = View.GONE
                binding.ivCover.visibility = View.VISIBLE

            Glide.with(requireContext())
                .load(uri)
                .centerCrop()
                .into(binding.ivCover)
        }

        setupToolbar()
        setupEdgeToEdgeInsets()
        setupListeners()
        setupTextWatchers()
        updateCreateButtonState()

        hideBottomNavigation()
        setupSystemBackNavigation()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            handleBackNavigation()
        }
    }


    private fun handleBackNavigation() {
        if (checkForChanges()) {
            showUnsavedChangesDialog {
                resetCoverState()
                findNavController().navigateUp()
            }
        } else {
            resetCoverState()
            findNavController().navigateUp()
        }
    }


    private fun setupSystemBackNavigation() {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }


    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }

            binding.btnCreate.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + 32.dpToPx()
            }

            insets
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun setupListeners() {
        binding.coverFrameLayout.setOnClickListener {
            checkAndRequestPermission()
        }

        binding.btnCreate.setOnClickListener {
            if (!isCreating) {
                createPlaylist()
            }
        }
    }

    private fun setupTextWatchers() {
        binding.tilName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateCreateButtonState()
            }
        })

        binding.tilDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun updateCreateButtonState() {
        val name = binding.tilName.text.toString().trim()
        val isEnabled = name.isNotEmpty()&& !isCreating

        binding.btnCreate.isEnabled = isEnabled

        if (isEnabled) {
            binding.btnCreate.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.blue_surface)
            )
            binding.btnCreate.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        } else {
            binding.btnCreate.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.grey)
            )
            binding.btnCreate.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.light_grey)
            )
        }
    }

    private fun checkAndRequestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openImagePicker()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun checkForChanges(): Boolean {
        val hasName = binding.tilName.text.toString().trim().isNotEmpty()
        val hasDescription = binding.tilDescription.text.toString().trim().isNotEmpty()
        val hasImage = selectedImageUri != null

        return hasName || hasDescription || hasImage
    }

    private fun resetCoverState() {
        selectedImageUri = null
        savedCoverImagePath = null

        binding.ivAddPhotoIcon.visibility = View.VISIBLE
        binding.ivCover.visibility = View.GONE
        binding.ivCover.setImageDrawable(null)
    }


    private fun showUnsavedChangesDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.unsaved_changes_title))
            .setMessage(getString(R.string.unsaved_changes_message))
            .setPositiveButton(getString(R.string.discard)) { dialog, _ ->
                dialog.dismiss()
                onConfirm()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private suspend fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            withContext(Dispatchers.IO) {
                val inputStream: InputStream? =
                    requireContext().contentResolver.openInputStream(uri)
                val fileName = "playlist_cover_${System.currentTimeMillis()}.jpg"
                val file = File(requireContext().filesDir, fileName)

                inputStream?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                file.absolutePath
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun createPlaylist() {
        if (isCreating) return
        val name = binding.tilName.text.toString().trim()
        val description = binding.tilDescription.text.toString().trim().takeIf { it.isNotEmpty() }

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.name_required), Toast.LENGTH_SHORT).show()
            return
        }

        isCreating = true
        updateCreateButtonState()
        binding.btnCreate.isEnabled = false

        lifecycleScope.launch {
            try {
                savedCoverImagePath = selectedImageUri?.let { saveImageToInternalStorage(it) }


                val playlist = Playlist(
                    name = name,
                    description = description,
                    coverImagePath = savedCoverImagePath,
                    trackIds = emptyList(),
                    trackCount = 0
                )

                val playlistId = viewModel.createPlaylist(playlist)

                trackToAdd?.let { track ->
                    viewModel.addTrackToPlaylist(playlistId, track)
                }

                val successMessage = getString(R.string.playlist_created, name)


                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        (activity as? RootActivity)?.showGlobalToast(successMessage)
                    }
                }


                delay(500)

                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        findNavController().navigateUp()
                    }
                    isCreating = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_creating_playlist),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                isCreating = false
                updateCreateButtonState()
            }
        }
    }

    private fun hideBottomNavigation() {
        (activity as? RootActivity)?.let { rootActivity ->
            rootActivity.hideBottomNavigationView()
        }
    }

    private fun showBottomNavigation() {
        (activity as? RootActivity)?.let { rootActivity ->
            rootActivity.showBottomNavigationView()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedImageUri?.let { uri ->
            outState.putParcelable("SELECTED_IMAGE_URI", uri)
        }
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigation()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavigation()
        trackToAdd = null
        _binding = null
        isCreating = false
    }
}