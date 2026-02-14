package com.example.playlistmaker.library.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaylistTracksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: PlaylistTrackEntity)

    @Query("SELECT * FROM playlist_tracks WHERE track_id = :trackId")
    suspend fun getTrackById(trackId: Int): PlaylistTrackEntity?

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE track_id = :trackId")
    suspend fun trackExists(trackId: Int): Boolean

    @Query("DELETE FROM playlist_tracks WHERE track_id = :trackId")
    suspend fun deleteTrackById(trackId: Int)

    @Query("SELECT * FROM playlist_tracks WHERE track_id IN (:trackIds)")
    suspend fun getTracksByIds(trackIds: List<Int>): List<PlaylistTrackEntity>

    @Query("SELECT * FROM playlist_tracks")
    suspend fun getAllTracks(): List<PlaylistTrackEntity>

    @Delete
    suspend fun deleteTrack(track: PlaylistTrackEntity): Int

}