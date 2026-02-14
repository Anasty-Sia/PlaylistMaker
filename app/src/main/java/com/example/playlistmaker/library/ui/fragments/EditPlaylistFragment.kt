package com.example.playlistmaker.library.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.library.ui.view_model.EditPlaylistUiState
import com.example.playlistmaker.library.ui.view_model.EditPlaylistViewModel
import com.example.playlistmaker.root.RootActivity
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class EditPlaylistFragment : NewPlaylistFragment() {

    private val args: EditPlaylistFragmentArgs by navArgs()

    private val editViewModel: EditPlaylistViewModel by viewModel {
        parametersOf(args.playlistId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupEditMode()
    }

    private fun observeViewModel() {
        editViewModel.uiState.onEach { state ->
            updateUi(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        editViewModel.uiState.onEach { state ->
            state.updateResult?.let { success ->
                if (success) {
                    val successMessage = getString(R.string.playlist_updated, state.name)
                    (activity as? RootActivity)?.showGlobalToast(successMessage)
                    resetCoverState()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_updating_playlist),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                editViewModel.clearUpdateResult()
            }

            state.error?.let { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                editViewModel.clearError()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updateUi(state: EditPlaylistUiState) {
        if (state.playlist != null) {
            loadPlaylistData(state)
        }

        binding.btnCreate.isEnabled = state.isFormValid && !state.isUpdating

        binding.btnCreate.text = if (state.isUpdating) {
            getString(R.string.saving)
        } else {
            getString(R.string.save)
        }
    }

    private fun loadPlaylistData(state: EditPlaylistUiState) {
        if (!state.coverImagePath.isNullOrEmpty()) {
            savedCoverImagePath = state.coverImagePath
            selectedImageUri = null

            binding.ivAddPhotoIcon.isVisible = false
            binding.ivCover.isVisible = true

            Glide.with(requireContext())
                .load(state.coverImagePath)
                .placeholder(R.drawable.ic_placeholder_45)
                .error(R.drawable.ic_placeholder_45)
                .centerCrop()
                .into(binding.ivCover)
        }

        binding.tilName.setText(state.name)
        binding.tilDescription.setText(state.description)

        updateCreateButtonState()
    }

    private fun setupEditMode() {
        binding.toolbar.title = getString(R.string.edit_playlist)
        binding.btnCreate.text = getString(R.string.save)
    }

    override fun handleBackNavigation() {
        if (editViewModel.hasChanges()) {
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
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.name_required), Toast.LENGTH_SHORT).show()
            return
        }

        isCreating = true
        updateCreateButtonState()

        lifecycleScope.launch {
            try {
                val newCoverPath = if (selectedImageUri != null) {
                    saveImageToInternalStorage(selectedImageUri!!)
                } else {
                    savedCoverImagePath
                }

                editViewModel.updateCoverImagePath(newCoverPath)
                editViewModel.updateName(name)
                editViewModel.updateDescription(binding.tilDescription.text.toString())

                editViewModel.updatePlaylist { success ->
                    if (!success) {
                        isCreating = false
                        updateCreateButtonState()
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
            }
        }
    }

    override fun onTextChanged() {
        super.onTextChanged()
        editViewModel.updateName(binding.tilName.text.toString())
        editViewModel.updateDescription(binding.tilDescription.text.toString())
    }
}