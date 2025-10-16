package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.TrackResponseDto
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.network.iTunesSearchAPI
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

class TrackRepositoryImpl(private val iTunesService: iTunesSearchAPI) : TrackRepository {

    override suspend fun searchTracks(query: String): List<Track> {
        try {
            val response: Response<TrackResponseDto> = iTunesService.search(query)
            if (response.isSuccessful) {
                val trackResponse = response.body()
                return trackResponse?.results?.let { trackDtos ->
                    TrackMapper.mapDtoListToDomain(trackDtos)
                } ?: emptyList()
            } else {
                return emptyList()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
           return emptyList()
        }
    }
}