package com.example.playlistmaker.ui.screens.playlist

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.io.File
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.view_model.PlaylistsViewModel
import com.example.playlistmaker.ui.utils.ImageStorageHelper
import com.example.playlistmaker.ui.utils.FileManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import android.widget.Toast
import com.example.playlistmaker.search.domain.model.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistScreen(
    onBackPressed: () -> Unit,
    onPlaylistCreated: (Long) -> Unit,
    trackToAdd: Track? = null,
    viewModel: PlaylistsViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var savedImagePath by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        FileManager.initPlaylistsDirectory(context)
    }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val hasPermission =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED


    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                lifecycleScope.launch {
                    val path = ImageStorageHelper.saveImageToInternalStorage(
                        context.contentResolver,
                        uri
                    )
                    savedImagePath = path
                }
            }
        }
    }

    fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(context, R.string.permission_required_for_image, Toast.LENGTH_SHORT)
                .show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.new_playlist),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(312.dp)
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .drawWithContent {
                        drawContent()
                        val borderColor = Color.Gray
                        drawRoundRect(
                            color = borderColor,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(10f, 10f),
                                    phase = 0f
                                )
                            ),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                    }
                    .clickable {
                        if (hasPermission) {
                            openImagePicker()
                        } else {
                            permissionLauncher.launch(permission)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (savedImagePath != null || selectedImageUri != null) {
                    AsyncImage(
                        model = if (savedImagePath != null) File(savedImagePath) else selectedImageUri,
                        contentDescription = stringResource(R.string.playlist_cover),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_photo_100),
                        contentDescription = stringResource(R.string.add_photo),
                        tint = Color.Unspecified, // сохраняем оригинальные цвета из drawable
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.playlist_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.playlist_description_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            )
            {
            Button(
                onClick = {
                    if (name.isNotBlank() && !isCreating) {
                        isCreating = true
                        lifecycleScope.launch {
                            try {
                                val trackIds = trackToAdd?.let { listOf(it.trackId) } ?: emptyList()
                                val playlist = Playlist(
                                    playlistId = 0,
                                    name = name.trim(),
                                    description = description.trim().takeIf { it.isNotBlank() },
                                    coverImagePath = savedImagePath,
                                    trackIds = trackIds,
                                    trackCount = trackIds.size
                                )
                                val playlistId = viewModel.createPlaylist(playlist)


                                trackToAdd?.let { track ->
                                    viewModel.addTrackToPlaylist(playlistId, track)
                                }

                                viewModel.refresh()

                                Toast.makeText(
                                    context,
                                    context.getString(R.string.playlist_created, name.trim()),
                                    Toast.LENGTH_SHORT
                                ).show()

                                onPlaylistCreated(playlistId)
                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    R.string.error_creating_playlist,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isCreating = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (name.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        Color(0xFF535459) // цвет из XML
                )
            ) {
                Text(
                    text = stringResource(R.string.create),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        }
    }
}