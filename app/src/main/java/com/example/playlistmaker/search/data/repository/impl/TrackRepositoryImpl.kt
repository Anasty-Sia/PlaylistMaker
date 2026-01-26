package com.example.playlistmaker.search.data.repository.impl

import com.example.playlistmaker.data.network.TrackResponseDto
import com.example.playlistmaker.data.network.iTunesSearchAPI
import com.example.playlistmaker.library.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.data.repository.mapper.TrackMapper
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response



class TrackRepositoryImpl(private val iTunesService: iTunesSearchAPI, private val gson: Gson,
                          private val favoriteTracksRepository: FavoriteTracksRepository) : TrackRepository {

    override fun searchTracks(query: String): Flow<List<Track>> = flow{
        try {
            val response: Response<TrackResponseDto> = iTunesService.search(query)
            if (response.isSuccessful) {
                val trackResponse = response.body()
                val tracks =trackResponse?.results?.let { trackDtos ->
                    TrackMapper.mapDtoListToDomain(trackDtos)
                } ?: emptyList()
                //Получаем список избранных треков
                val favoriteTrackIds = favoriteTracksRepository.getFavoriteTrackIds()

                // Отмечаем избранные треки
                val tracksWithFavorites = tracks.map { track ->
                    track.copy(isFavorite = track.trackId in favoriteTrackIds)
                }
                emit(tracksWithFavorites)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}