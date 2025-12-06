package com.example.playlistmaker.settings.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModel()

    private var recreateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStatusBarPadding()
        setupViews()
        observeViewModel()
    }

    private fun setupStatusBarPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.backB.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }

            insets
        }
    }

    private fun setupViews() {
        binding.darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeSwitchChanged(isChecked)
            setDarkThemeWithDelay(isChecked)
        }

        binding.iconShare.setOnClickListener {
            viewModel.onShareClicked()
        }

        binding.iconSupport.setOnClickListener {
            viewModel.onSupportClicked()
        }

        binding.termsOfService.setOnClickListener {
            viewModel.onTermsClicked()
        }
    }

    private fun observeViewModel() {
        viewModel.darkThemeEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.darkThemeSwitch.setOnCheckedChangeListener(null)
            binding.darkThemeSwitch.isChecked = isEnabled
            binding.darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onThemeSwitchChanged(isChecked)
                setDarkThemeWithDelay(isChecked)
            }
        }
    }

    private fun setDarkThemeWithDelay(enabled: Boolean) {
        recreateJob?.cancel()

        recreateJob = scope.launch {
            AppCompatDelegate.setDefaultNightMode(
                if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            delay(300)

            if (isAdded && activity != null) {
                requireActivity().recreate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recreateJob?.cancel()
        _binding = null
    }
}