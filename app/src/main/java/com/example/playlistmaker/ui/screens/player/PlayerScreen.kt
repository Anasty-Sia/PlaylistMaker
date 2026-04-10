package com.example.playlistmaker.ui.screens.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.player.domain.model.PlaybackState
import com.example.playlistmaker.player.service.PlayerService
import com.example.playlistmaker.player.service.PlayerServiceConnector
import com.example.playlistmaker.player.ui.view_model.PlayerState
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    track: Track,
    onBackPressed: () -> Unit,
    onNavigateToCreatePlaylist: () -> Unit = {},
    viewModel: PlayerViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val playerState by viewModel.playerState.observeAsState(initial = PlayerState())
    val playlists by viewModel.playlists.observeAsState(initial = emptyList())
    val showBottomSheet by viewModel.showBottomSheet.observeAsState(initial = false)

    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as PlayerService.PlayerBinder
                val playerService = binder.getService()

                val connector = object : PlayerServiceConnector {
                    override fun preparePlayer() = playerService.preparePlayer()
                    override fun startPlayer() = playerService.startPlayer()
                    override fun pausePlayer() = playerService.pausePlayer()
                    override fun stopPlayback() = playerService.stopPlayback()
                    override fun getPlaybackState() = playerService.getPlaybackState()
                    override fun getCurrentPosition() = playerService.getCurrentPosition()
                    override fun getDuration() = playerService.getDuration()
                    override fun showForegroundNotification() = playerService.showForegroundNotification()
                    override fun hideForegroundNotification() = playerService.hideForegroundNotification()
                    override fun setStateListener(listener: PlayerService.PlayerStateListener?) {
                        playerService.setStateListener(listener)
                    }
                }

                viewModel.bindService(connector)
                viewModel.setTrack(track)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                viewModel.unbindService()
            }
        }

        val intent = Intent(context, PlayerService::class.java).apply {
            putExtra("track", track)
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        onDispose {
            viewModel.unbindService()
            context.unbindService(serviceConnection)
            viewModel.releasePlayer()
        }
    }


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.setAppForegroundState(true)
                Lifecycle.Event.ON_PAUSE -> viewModel.setAppForegroundState(false)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.player),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(26.dp))

                AsyncImage(
                    model = track.getHighResArtworkUrl() ?: track.artworkUrl100,
                    contentDescription = track.trackName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_placeholder_45),
                    error = painterResource(R.drawable.ic_placeholder_45)
                )

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = track.trackName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))


                Text(
                    text = track.artistName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(30.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = { viewModel.showPlaylistBottomSheet() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_to_playlist_51),
                            contentDescription = stringResource(R.string.add_to_playlist),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                Log.d("PlayerScreen", "Play/Pause clicked, state: ${playerState.playbackState}")
                                viewModel.playPause()
                            },
                            modifier = Modifier.size(100.dp),
                            enabled = playerState.playbackState !is PlaybackState.ERROR
                        ) {
                            Icon(
                                painter = if (playerState.playbackState is PlaybackState.PLAYING) {
                                    painterResource(R.drawable.ic_pause)
                                } else {
                                    painterResource(R.drawable.ic_play_arrow)
                                },
                                contentDescription = if (playerState.playbackState is PlaybackState.PLAYING) {
                                    stringResource(R.string.pause)
                                } else {
                                    stringResource(R.string.play)
                                },
                                modifier = Modifier.size(100.dp),
                                tint = MaterialTheme.colorScheme.onSurface  // Светлая тема #1A1B22, темная #FFFFFF
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = if (playerState.isFavorite) {
                                painterResource(R.drawable.ic_favorite_filled_51)
                            } else {
                                painterResource(R.drawable.ic_favorite_border_51)
                            },
                            contentDescription = stringResource(R.string.favorite),
                            tint = if (playerState.isFavorite) {
                                Color(0xFFF56B6C)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTime(playerState.currentPosition),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp))

                PlayerInfoRow(
                    label = stringResource(R.string.duration),
                    value = track.getFormattedTime()
                )

                track.collectionName?.takeIf { it.isNotEmpty() }?.let { collectionName ->
                    PlayerInfoRow(
                        label = stringResource(R.string.album),
                        value = collectionName
                    )
                }

                track.getReleaseYear()?.takeIf { it.isNotEmpty() }?.let { releaseYear ->
                    PlayerInfoRow(
                        label = stringResource(R.string.year),
                        value = releaseYear
                    )
                }

                track.primaryGenreName?.takeIf { it.isNotEmpty() }?.let { genre ->
                    PlayerInfoRow(
                        label = stringResource(R.string.genre),
                        value = genre
                    )
                }


                track.country?.takeIf { it.isNotEmpty() }?.let { country ->
                    PlayerInfoRow(
                        label = stringResource(R.string.country),
                        value = country
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            if (showBottomSheet) {
                PlaylistBottomSheet(
                    playlists = playlists,
                    currentTrackId = track.trackId,
                    onDismiss = { viewModel.hidePlaylistBottomSheet() },
                    onPlaylistClick = { playlist ->
                        coroutineScope.launch {
                            viewModel.addTrackToPlaylist(playlist.playlistId, track)
                        }
                    },
                    onCreatePlaylistClick = {
                        viewModel.hidePlaylistBottomSheet()
                        onNavigateToCreatePlaylist()
                    }
                )
            }
        }
    }
}

@Composable
fun PlayerInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 9.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.wrapContentWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.wrapContentWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistBottomSheet(
    playlists: List<Playlist>,
    currentTrackId: Int,
    onDismiss: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.add_to_playlist),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_playlists),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(playlists) { playlist ->
                        PlaylistBottomSheetItem(
                            playlist = playlist,
                            isTrackInPlaylist = playlist.trackIds.contains(currentTrackId),
                            onClick = { onPlaylistClick(playlist) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onCreatePlaylistClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_playlist),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PlaylistBottomSheetItem(
    playlist: Playlist,
    isTrackInPlaylist: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isTrackInPlaylist) { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_placeholder_45),
                contentDescription = null,
                modifier = Modifier.size(45.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(R.plurals.tracks_count, playlist.trackCount, playlist.trackCount),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isTrackInPlaylist) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = stringResource(R.string.added),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatTime(milliseconds: Int): String {
    if (milliseconds <= 0) return "00:00"
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}