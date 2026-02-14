package com.example.playlistmaker.library.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.library.ui.view_model.EditPlaylistViewModel
import com.example.playlistmaker.root.RootActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditPlaylistFragment : NewPlaylistFragment() {

    private val args: EditPlaylistFragmentArgs by navArgs()

    private val editViewModel: EditPlaylistViewModel by viewModel {
        parametersOf(args.playlist)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editViewModel.isFormValid.observe(viewLifecycleOwner) { isValid ->
            binding.btnCreate.isEnabled = isValid && !isCreating
        }

        setupEditMode()
        loadPlaylistData()
    }

    private fun setupEditMode() {
        binding.toolbar.title = getString(R.string.edit_playlist)
        binding.btnCreate.text = getString(R.string.save)
    }

    private fun loadPlaylistData() {
        args.playlist?.let { playlist ->
            if (!playlist.coverImagePath.isNullOrEmpty()) {
                savedCoverImagePath = playlist.coverImagePath
                selectedImageUri = null

                binding.ivAddPhotoIcon.visibility = View.GONE
                binding.ivCover.visibility = View.VISIBLE

                Glide.with(requireContext())
                    .load(playlist.coverImagePath)
                    .placeholder(R.drawable.ic_placeholder_45)
                    .error(R.drawable.ic_placeholder_45)
                    .centerCrop()
                    .into(binding.ivCover)
            }


            binding.tilName.setText(playlist.name)
            binding.tilDescription.setText(playlist.description ?: "")

            updateCreateButtonState()
        }
    }

    override fun handleBackNavigation() {
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

    override fun createPlaylist() {
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
                val newCoverPath = if (selectedImageUri != null) {
                    saveImageToInternalStorage(selectedImageUri!!)
                } else {
                    savedCoverImagePath
                }


                editViewModel.updatePlaylist(
                    name = name,
                    description = description,
                    coverImagePath = newCoverPath
                ) { success ->
                    if (success) {
                        val successMessage = getString(R.string.playlist_updated, name)

                        (activity as? RootActivity)?.showGlobalToast(successMessage)


                        resetCoverState()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_updating_playlist),
                            Toast.LENGTH_SHORT
                        ).show()
                        isCreating = false
                        updateCreateButtonState()
                        binding.btnCreate.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_updating_playlist),
                    Toast.LENGTH_SHORT
                ).show()
                isCreating = false
                updateCreateButtonState()
                binding.btnCreate.isEnabled = true
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("saved_cover_path", savedCoverImagePath)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            savedCoverImagePath = it.getString("saved_cover_path")
        }
    }
}