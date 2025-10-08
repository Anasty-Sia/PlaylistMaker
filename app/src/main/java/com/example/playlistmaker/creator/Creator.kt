package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.data.network.iTunesSearchAPI
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.interactor.SettingsInteractor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    private const val ITUNES_BASE_URL = "https://itunes.apple.com"

    private fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideiTunesService(): iTunesSearchAPI {
        return provideRetrofit().create(iTunesSearchAPI::class.java)
    }

    private fun provideTrackRepository(): TrackRepositoryImpl {
        return TrackRepositoryImpl(provideiTunesService())
    }

    private fun provideSearchHistoryRepository(context: Context): SearchHistoryRepositoryImpl {
        return SearchHistoryRepositoryImpl(context)
    }

    private fun provideSettingsRepository(context: Context): SettingsRepositoryImpl {
        return SettingsRepositoryImpl(context)
    }

    fun provideSearchInteractor(context: Context): SearchInteractor {
        return SearchInteractor(provideTrackRepository())
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractor(provideSearchHistoryRepository(context))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractor(provideSettingsRepository(context))
    }
}