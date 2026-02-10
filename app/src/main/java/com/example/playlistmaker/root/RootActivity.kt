package com.example.playlistmaker.root

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRootBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
    }

    fun animateBottomNavigationView() {
        binding.bottomNavigationView.visibility = View.GONE
    }

    fun showBottomNavigationView() {
        binding.bottomNavigationView.visibility = View.VISIBLE
    }

    fun hideBottomNavigationView() {
        binding.bottomNavigationView.visibility = View.GONE
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.rootFragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.libraryFragment -> {
                    if (navController.currentDestination?.id != R.id.libraryFragment) {
                        navController.navigate(R.id.libraryFragment)
                    }
                }
                R.id.searchFragment -> {
                    if (navController.currentDestination?.id != R.id.searchFragment) {
                        navController.navigate(R.id.searchFragment)
                    }
                }
                R.id.settingsFragment -> {
                    if (navController.currentDestination?.id != R.id.settingsFragment) {
                        navController.navigate(R.id.settingsFragment)
                    }
                }
            }
            true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavigationSelection(destination)
        }
    }

    private fun updateBottomNavigationSelection(destination: NavDestination) {
        when (destination.id) {
            R.id.libraryFragment -> {
                binding.bottomNavigationView.menu.findItem(R.id.libraryFragment).isChecked = true
            }
            R.id.searchFragment -> {
                binding.bottomNavigationView.menu.findItem(R.id.searchFragment).isChecked = true
            }
            R.id.settingsFragment -> {
                binding.bottomNavigationView.menu.findItem(R.id.settingsFragment).isChecked = true
            }
        }
    }

    fun showGlobalToast(message: String) {
        runOnUiThread {

            val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
            val existingToast = rootView.findViewById<View>(R.id.toast_layout)
            existingToast?.let {
                rootView.removeView(it)
            }

            val toastView = LayoutInflater.from(this).inflate(R.layout.custom_toast, null)
            toastView.id = R.id.toast_layout

            val textView = toastView.findViewById<TextView>(R.id.toast_text)
            textView.text = message

            textView.visibility = View.VISIBLE

            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.toast_height)
            ).apply {
                val marginHorizontal = resources.getDimensionPixelSize(R.dimen.toast_margin_horizontal)
                val marginBottom = resources.getDimensionPixelSize(R.dimen.toast_margin_bottom)
                setMargins(marginHorizontal, 0, marginHorizontal, marginBottom)
                gravity = Gravity.BOTTOM
            }

            rootView.addView(toastView, params)

            Handler(Looper.getMainLooper()).postDelayed({
                if (toastView.parent != null) {
                    toastView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            (toastView.parent as? ViewGroup)?.removeView(toastView)
                        }
                        .start()
                }
            }, 1500)


        }
    }
}