package com.example.playlistmaker.search.data.repository.impl

import com.example.playlistmaker.data.network.TrackResponseDto
import com.example.playlistmaker.data.network.iTunesSearchAPI
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.data.repository.mapper.TrackMapper
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