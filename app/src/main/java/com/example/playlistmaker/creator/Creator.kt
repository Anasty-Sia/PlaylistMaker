package com.example.playlistmaker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.data.network.iTunesSearchAPI
import com.example.playlistmaker.player.data.repository.impl.PlayerRepositoryImpl
import com.example.playlistmaker.search.data.repository.impl.SearchHistoryRepositoryImpl
import com.example.playlistmaker.settings.data.repository.impl.SettingsRepositoryImpl
import com.example.playlistmaker.search.data.repository.impl.TrackRepositoryImpl
import com.example.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.player.domain.interactor.PlayerInteractor
import com.example.playlistmaker.player.domain.interactor.impl.PlayerInteractorImpl
import com.example.playlistmaker.player.domain.repository.PlayerRepository
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.domain.interactor.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.search.domain.interactor.impl.SearchInteractorImpl
import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.ui.view_model.SearchViewModel
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractorInterface
import com.example.playlistmaker.settings.domain.interactor.SharingInteractor
import com.example.playlistmaker.settings.domain.interactor.impl.SharingInteractorImpl
import com.example.playlistmaker.settings.domain.repository.SettingsRepository
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
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

    private fun providePlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }

    private fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(providePlayerRepository())
    }

    fun providePlayerViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PlayerViewModel(providePlayerInteractor()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    private fun provideTrackRepository(): TrackRepository {
        return TrackRepositoryImpl(provideiTunesService())
    }

    private fun provideSearchInteractor(): SearchInteractor {
        return SearchInteractorImpl(provideTrackRepository())
    }

    private fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(context)
    }

    private fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(provideSearchHistoryRepository(context))
    }


    fun provideSearchViewModelFactory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return SearchViewModel(
                        provideSearchInteractor(),
                        provideSearchHistoryInteractor(context)
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private fun provideSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

     fun provideSettingsInteractor(context: Context): SettingsInteractorInterface {
        return SettingsInteractor(provideSettingsRepository(context))
    }

    private fun provideSharingInteractor(context: Context): SharingInteractor {
        return SharingInteractorImpl(context)
    }

    fun provideSettingsViewModelFactory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    provideSettingsInteractor(context),
                    provideSharingInteractor(context)
                ) as T
            }
        }
    }
}
