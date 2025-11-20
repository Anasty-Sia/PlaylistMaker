package com.example.playlistmaker.search.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale

@Parcelize
data class Track(
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null,
    val previewUrl: String? = null
): Parcelable {
    fun getFormattedTime(): String {
        return if (trackTimeMillis != null) {
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis)
        } else {
            ""
        }
    }

    fun getHighResArtworkUrl(): String? {
        return artworkUrl100?.replace("100x100bb", "512x512bb")
    }

    fun getReleaseYear(): String? {
        return releaseDate?.take(4)
    }
}