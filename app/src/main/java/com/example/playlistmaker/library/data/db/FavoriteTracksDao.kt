package com.example.playlistmaker.library.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTracksDao {

    @Insert
    suspend fun insertTrack(track: FavoriteTrackEntity)

    @Delete
    suspend fun deleteTrack(track: FavoriteTrackEntity)

    @Query("SELECT * FROM favorite_tracks ORDER BY addition_date DESC")
    fun getAllTracks(): Flow<List<FavoriteTrackEntity>>

    @Query("SELECT track_id FROM favorite_tracks")
    suspend fun getAllFavoriteTrackIds(): List<Int>

    @Query("SELECT COUNT(*) FROM favorite_tracks WHERE track_id = :trackId")
    suspend fun isTrackFavorite(trackId: Int): Boolean

    // Добавьте метод для проверки существования трека по trackId
    @Query("SELECT * FROM favorite_tracks WHERE track_id = :trackId")
    suspend fun getTrackById(trackId: Int): FavoriteTrackEntity?
}