package com.example.playlistmaker.search.domain.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelizet
import java.util.Locale
import java.util.concurrent.TimeUnit

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
    val previewUrl: String? = null,
    var isFavorite: Boolean = false
): Parcelable {

    fun getFormattedTime(): String {
        return if (trackTimeMillis != null) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(trackTimeMillis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(trackTimeMillis) % 60
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        } else {
            "00:00"
        }
    }

    fun getHighResArtworkUrl(): String? {
        return artworkUrl100?.replace("100x100bb", "512x512bb")
    }


    fun getReleaseYear(): String? {
        return releaseDate?.take(4)
    }
}