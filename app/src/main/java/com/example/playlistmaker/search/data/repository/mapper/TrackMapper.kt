package com.example.playlistmaker.search.data.repository.mapper

import com.example.playlistmaker.data.network.TrackDto
import com.example.playlistmaker.search.domain.model.Track

object TrackMapper {
    fun mapDtoToDomain(dto: TrackDto): Track {
        return Track(
            trackId = dto.trackId,
            trackName = dto.trackName ?: "",
            artistName = dto.artistName ?: "",
            trackTimeMillis = dto.trackTimeMillis,
            artworkUrl100 = dto.artworkUrl100,
            collectionName = dto.collectionName,
            releaseDate = dto.releaseDate,
            primaryGenreName = dto.primaryGenreName,
            country = dto.country,
            previewUrl = dto.previewUrl
        )
    }

    fun mapDtoListToDomain(dtoList: List<TrackDto>): List<Track> {
        return dtoList.map { mapDtoToDomain(it) }
    }
}