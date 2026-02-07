package com.example.playlistmaker.library.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "cover_image_path")
    val coverImagePath: String? = null,

    @ColumnInfo(name = "track_ids_json")
    val trackIdsJson: String = "[]",

    @ColumnInfo(name = "track_count")
    val trackCount: Int = 0
) {
    fun getTrackIds(): List<Int> {
        return try {
            Gson().fromJson(trackIdsJson, object : TypeToken<List<Int>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun createTrackIdsJson(trackIds: List<Int>): String {
            return Gson().toJson(trackIds)
        }
    }
}