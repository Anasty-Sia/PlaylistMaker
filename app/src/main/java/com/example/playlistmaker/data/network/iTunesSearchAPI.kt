package com.example.playlistmaker.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface iTunesSearchAPI {

    @GET("search")
    suspend fun search(
        @Query("term") text: String,
        @Query("entity") entity: String = "song"
    ): Response<TrackResponseDto>
}