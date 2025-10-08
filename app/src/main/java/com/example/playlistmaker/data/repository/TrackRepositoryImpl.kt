package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.TrackResponseDto
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.network.iTunesSearchAPI
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(private val iTunesService: iTunesSearchAPI) : TrackRepository {

    override suspend fun searchTracks(query: String): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response: retrofit2.Response<TrackResponseDto> = iTunesService.search(query).execute()
            if (response.isSuccessful) {
                val trackResponse = response.body()
                trackResponse?.results?.let { trackDtos ->
                    return@withContext TrackMapper.mapDtoListToDomain(trackDtos)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}