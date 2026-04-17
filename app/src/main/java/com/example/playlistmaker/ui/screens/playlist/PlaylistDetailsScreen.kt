package com.example.playlistmaker.ui.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.io.File
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.view_model.PlaylistDetailsViewModel
import com.example.playlistmaker.library.ui.view_model.PlaylistDetailsState
import com.example.playlistmaker.search.domain.model.Track
import org.koin.androidx.compose.koinViewModel
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(
    playlistId: Long,
    onBackPressed: () -> Unit,
    onEditPlaylist: (Long) -> Unit,
    onTrackClick: (Track) -> Unit,
    viewModel: PlaylistDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.observeAsState(initial = PlaylistDetailsState())
    val shareResult by viewModel.shareResult.observeAsState()
    val showEmptyToast by viewModel.showEmptyPlaylistToast.observeAsState(initial = false)
    val navigateBack by viewModel.navigateBack.observeAsState(initial = false)
    val context = LocalContext.current

    var showDeleteTrackDialog by remember { mutableStateOf<Track?>(null) }
    var showDeletePlaylistDialog by remember { mutableStateOf(false) }
    var showMenuSheet by remember { mutableStateOf(false) }


    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { true }
    )
    var showTracksBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()



    LaunchedEffect(playlistId) {
        if (playlistId != 0L) {
            viewModel.loadPlaylist(playlistId)
            viewModel.loadPlaylistTracks(playlistId)
        }
    }

    LaunchedEffect(navigateBack) {
        if (navigateBack) {
            onBackPressed()
        }
    }

    LaunchedEffect(shareResult) {
        shareResult?.let { text ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_playlist_via)))
        }
    }

    LaunchedEffect(showEmptyToast) {
        if (state.tracks.isNotEmpty() && bottomSheetState.currentValue == SheetValue.Hidden)  {
            Toast.makeText(context, context.getString(R.string.empty_playlist_share_message), Toast.LENGTH_SHORT).show()
        }
    }


    LaunchedEffect(state.tracks) {
        if (state.tracks.isNotEmpty()) {
            showTracksBottomSheet = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_arrow_24),
                            contentDescription = stringResource(R.string.back),
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE6E8EB)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFE6E8EB))
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val playlist = state.playlist
                val tracks = state.tracks

                if (playlist == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.playlist_not_found),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                                .padding(24.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (!playlist.coverImagePath.isNullOrEmpty() && File(playlist.coverImagePath).exists()) {
                                AsyncImage(
                                    model = File(playlist.coverImagePath),
                                    contentDescription = playlist.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.ic_placeholder_45),
                                    error = painterResource(R.drawable.ic_placeholder_45)
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_placeholder_45),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color.LightGray
                                    )
                                }
                            }
                        }

                        Text(
                            text = playlist.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Описание
                        if (!playlist.description.isNullOrEmpty()) {
                            Text(
                                text = playlist.description,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = state.totalDuration,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "•",
                                fontSize = 14.sp,
                                color =  Color.Black
                            )
                            Text(
                                text = pluralStringResource(R.plurals.tracks_count, tracks.size, tracks.size),
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.sharePlaylist() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_share),
                                    contentDescription = stringResource(R.string.share),
                                    tint = Color.Black,

                                )
                            }
                            IconButton(
                                onClick = { showMenuSheet = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_more_vert),
                                    contentDescription = stringResource(R.string.menu),
                                    tint =Color.Black
                                )
                            }
                        }


                    }
                }
            }

            DropdownMenu(
                expanded = showMenuSheet,
                onDismissRequest = { showMenuSheet = false  }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.share)) },
                    onClick = {
                        showMenuSheet = false
                        viewModel.sharePlaylist()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit_info)) },
                    onClick = {
                        showMenuSheet = false
                        onEditPlaylist(playlistId)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete_playlist)) },
                    onClick = {
                        showMenuSheet = false
                        showDeletePlaylistDialog = true
                    }
                )
            }
        }
    }


    if (showTracksBottomSheet && state.tracks.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    if (bottomSheetState.currentValue == SheetValue.Expanded) {
                        bottomSheetState.partialExpand()
                    } else {
                        showTracksBottomSheet = false
                    }
                }
            },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 500.dp)
                    .padding(vertical = 8.dp)
            ) {
                LazyColumn {
                    items(state.tracks) { track ->
                        TrackItem(
                            track = track,
                            onClick = {
                                onTrackClick(track)
                            },
                            onLongClick = { showDeleteTrackDialog = track }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 60.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }

    showDeleteTrackDialog?.let { track ->
        AlertDialog(
            onDismissRequest = { showDeleteTrackDialog = null },
            title = { Text(stringResource(R.string.delete_track_title)) },
            text = { Text(stringResource(R.string.delete_track_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTrackFromPlaylist(track.trackId)
                        Toast.makeText(context, context.getString(R.string.track_deleted), Toast.LENGTH_SHORT).show()
                        showDeleteTrackDialog = null
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTrackDialog = null }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }


    if (showDeletePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePlaylistDialog = false },
            title = { Text(stringResource(R.string.delete_playlist_title)) },
            text = { Text(stringResource(R.string.delete_playlist_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlaylist()
                        showDeletePlaylistDialog = false
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePlaylistDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.artworkUrl100,
            contentDescription = track.trackName,
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_placeholder_45),
            error = painterResource(R.drawable.ic_placeholder_45)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = track.trackName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.artistName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = " • ",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = track.getFormattedTime(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Icon(
            painter = painterResource(R.drawable.ic_arrow_forward_24),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 12.dp),
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}