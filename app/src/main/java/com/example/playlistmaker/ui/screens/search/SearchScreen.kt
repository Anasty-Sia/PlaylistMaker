package com.example.playlistmaker.ui.screens.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.view_model.SearchState
import com.example.playlistmaker.search.ui.view_model.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onTrackClick: (Track) -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    var searchText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current


    LaunchedEffect(Unit) {
        try {
            viewModel.loadSearchHistory()
        } catch (e: Exception) {
            Log.e("SearchScreen", "Failed to load history", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.search),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                viewModel.searchDebounced(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = {
                        searchText = ""
                        viewModel.searchDebounced("")
                        focusManager.clearFocus()
                    }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                if (searchText.isNotEmpty()) {
                    viewModel.searchDebounced(searchText)
                }
            }),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )


        when (searchState) {
            is SearchState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is SearchState.Content -> {
                val tracks = (searchState as SearchState.Content).tracks
                if (tracks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_found)
                        )
                    }
                } else {
                    LazyColumn {
                        items(tracks, key = { it.trackId }) { track ->
                            SimpleTrackItem(
                                track = track,
                                onClick = {
                                    viewModel.addTrackToHistory(track)
                                    onTrackClick(track)
                                }
                            )
                        }
                    }
                }
            }

            is SearchState.History -> {
                val tracks = (searchState as SearchState.History).tracks
                if (tracks.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.search_History),
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(onClick = {
                                viewModel.clearSearchHistory()
                            }) {
                                Text(
                                    text = stringResource(R.string.clear_history)
                                )
                            }
                        }
                        LazyColumn {
                            items(tracks, key = { it.trackId }) { track ->
                                SimpleTrackItem(
                                    track = track,
                                    onClick = {
                                        viewModel.addTrackToHistory(track)
                                        onTrackClick(track)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.story_empty)
                        )
                    }
                }
            }

            is SearchState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (searchState as SearchState.Error).message,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            if (searchText.isNotEmpty()) {
                                viewModel.searchDebounced(searchText)
                            } else {
                                viewModel.loadSearchHistory()
                            }
                        }) {

                            Text(
                                text = stringResource(R.string.repeat)
                            )
                        }
                    }
                }
            }

            is SearchState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text((searchState as SearchState.Empty).message)
                }
            }

            is SearchState.Default -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("")
                }
            }
        }
    }
}

@Composable
fun SimpleTrackItem(
    track: Track,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.artworkUrl100,
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_placeholder_45),
            error = painterResource(R.drawable.ic_placeholder_45)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = track.trackName ?: "Unknown",
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = track.artistName ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}