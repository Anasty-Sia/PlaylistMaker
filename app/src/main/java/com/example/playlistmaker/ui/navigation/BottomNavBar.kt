package com.example.playlistmaker.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.playlistmaker.R

sealed class Screen(val route: String, val title: Int, val icon: ImageVector) {
    data object Search : Screen("search", R.string.search, Icons.Default.Search)
    data object Library : Screen("library", R.string.library, Icons.Default.LibraryMusic)
    data object Settings : Screen("settings", R.string.settings, Icons.Default.Settings)

    companion object {
        val entries = listOf(Search, Library, Settings)
    }
}

@Composable
fun BottomNavBar(
    navController: NavController,
    currentRoute: Screen,
    onTabSelected: (Screen) -> Unit
) {
    val items = listOf(
        Screen.Search,
        Screen.Library,
        Screen.Settings
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = stringResource(screen.title),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(stringResource(screen.title)) },
                selected = currentRoute.route == screen.route,
                onClick = { onTabSelected(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}