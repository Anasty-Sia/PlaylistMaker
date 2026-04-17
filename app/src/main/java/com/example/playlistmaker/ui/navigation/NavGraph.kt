package com.example.playlistmaker.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.ui.screens.library.LibraryScreen
import com.example.playlistmaker.ui.screens.player.PlayerScreen
import com.example.playlistmaker.ui.screens.playlist.CreatePlaylistScreen
import com.example.playlistmaker.ui.screens.playlist.EditPlaylistScreen
import com.example.playlistmaker.ui.screens.playlist.PlaylistDetailsScreen
import com.example.playlistmaker.ui.screens.search.SearchScreen
import com.example.playlistmaker.ui.screens.settings.SettingsScreen
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun PlaylistMakerNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Library.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route?.substringBefore("?") ?: Screen.Library.route

    val bottomBarVisible = when (currentRoute) {
        Screen.Search.route,
        Screen.Library.route,
        Screen.Settings.route -> true
        else -> false
    }

    val currentScreen = when (currentRoute) {
        Screen.Search.route -> Screen.Search
        Screen.Library.route -> Screen.Library
        Screen.Settings.route -> Screen.Settings
        else -> Screen.Library
    }

    if (bottomBarVisible) {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    navController = navController,
                    currentRoute = currentScreen,
                    onTabSelected = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Search.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                addComposables(navController)
            }
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            addComposables(navController)
        }
    }
}

private fun NavGraphBuilder.addComposables(navController: NavHostController) {
    composable(Screen.Search.route) {
        SearchScreen(
            onTrackClick = { track ->
                navigateToPlayer(navController, track)
            }
        )
    }

    composable(Screen.Library.route) {
        LibraryScreen(
            onPlaylistClick = { playlist ->
                navController.navigate("playlist_details/${playlist.playlistId}")
            },
            onTrackClick = { track ->
                navigateToPlayer(navController, track)
            },
            onCreatePlaylistClick = {
                navController.navigate("create_playlist")
            }
        )
    }

    composable(Screen.Settings.route) {
        SettingsScreen()
    }

    composable("create_playlist") {
        CreatePlaylistScreen(
            onBackPressed = { navController.popBackStack() },
            onPlaylistCreated = { playlistId ->
                navController.popBackStack()
                navController.navigate("playlist_details/$playlistId")
            },
            trackToAdd = null
        )
    }

    composable(
        route = "player/{trackJson}",
        arguments = listOf(navArgument("trackJson") { type = NavType.StringType })
    ) { backStackEntry ->
        val trackJson = backStackEntry.arguments?.getString("trackJson")
        val track = parseTrackFromJson(trackJson)

        if (track != null) {
            PlayerScreen(
                track = track,
                onBackPressed = { navController.popBackStack() },
                onNavigateToCreatePlaylist = {
                    val trackJson = URLEncoder.encode(Gson().toJson(track), "UTF-8")
                    navController.navigate("create_playlist_with_track/$trackJson")
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    composable(
        route = "create_playlist_with_track/{trackJson}",
        arguments = listOf(navArgument("trackJson") { type = NavType.StringType })
    ) { backStackEntry ->
        val trackJson = backStackEntry.arguments?.getString("trackJson")
        val track = parseTrackFromJson(trackJson)
        val context = androidx.compose.ui.platform.LocalContext.current

        CreatePlaylistScreen(
            onBackPressed = { navController.popBackStack() },
            onPlaylistCreated = { playlistId ->
                navController.popBackStack()
                Toast.makeText(context, "Трек добавлен в плейлист", Toast.LENGTH_SHORT).show()
            },
            trackToAdd = track
        )
    }

    composable(
        route = "playlist_details/{playlistId}",
        arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L

        PlaylistDetailsScreen(
            playlistId = playlistId,
            onBackPressed = { navController.popBackStack() },
            onEditPlaylist = { id ->
                navController.navigate("edit_playlist/$id")
            },
            onTrackClick = { track ->
                navigateToPlayer(navController, track)
            }
        )
    }

    composable(
        route = "edit_playlist/{playlistId}",
        arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L

        EditPlaylistScreen(
            playlistId = playlistId,
            onBackPressed = { navController.popBackStack() },
            onPlaylistUpdated = {
                navController.popBackStack()
                navController.navigate("playlist_details/$playlistId") {
                    popUpTo("playlist_details/$playlistId") { inclusive = true }
                }
            }
        )
    }
}

private fun navigateToPlayer(navController: NavHostController, track: Track) {
    try {
        val json = URLEncoder.encode(Gson().toJson(track), "UTF-8")
        navController.navigate("player/$json")
    } catch (e: Exception) {
    }
}

private fun parseTrackFromJson(trackJson: String?): Track? {
    return try {
        if (trackJson != null) {
            Gson().fromJson(URLDecoder.decode(trackJson, "UTF-8"), Track::class.java)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}