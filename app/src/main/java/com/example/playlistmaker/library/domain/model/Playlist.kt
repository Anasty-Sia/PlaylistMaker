package com.example.playlistmaker.library.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val playlistId: Long = 0,
    val name: String,
    val description: String? = null,
    val coverImagePath: String? = null,
    val trackIds: List<Int> = emptyList(),
    val trackCount: Int = 0
): Parcelable