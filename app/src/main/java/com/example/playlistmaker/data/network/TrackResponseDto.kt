package com.example.playlistmaker.data.network

import com.google.gson.annotations.SerializedName

data class TrackResponseDto(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<TrackDto>
)