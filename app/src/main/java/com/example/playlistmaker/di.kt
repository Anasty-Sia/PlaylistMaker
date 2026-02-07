package com.example.playlistmaker.di

import android.media.MediaPlayer
import com.example.playlistmaker.data.network.iTunesSearchAPI
import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.data.repository.impl.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.library.data.repository.impl.PlaylistsRepositoryImpl
import com.example.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.library.domain.interactor.PlaylistsInteractor
import com.example.playlistmaker.library.domain.interactor.impl.FavoriteTracksInteractorImpl
import com.example.playlistmaker.library.domain.interactor.impl.PlaylistsInteractorImpl
import com.example.playlistmaker.library.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.library.domain.repository.PlaylistsRepository
import com.example.playlistmaker.library.ui.view_model.FavoriteTracksViewModel
import com.example.playlistmaker.library.ui.view_model.PlaylistsViewModel
import com.example.playlistmaker.player.data.repository.impl.PlayerRepositoryImpl
import com.example.playlistmaker.player.domain.interactor.PlayerInteractor
import com.example.playlistmaker.player.domain.interactor.impl.PlayerInteractorImpl
import com.example.playlistmaker.player.domain.repository.PlayerRepository
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.data.repository.impl.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.data.repository.impl.TrackRepositoryImpl
import com.example.playlistmaker.search.data.repository.mapper.TrackMapper
import com.example.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.search.domain.interactor.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.search.domain.interactor.impl.SearchInteractorImpl
import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.ui.view_model.SearchViewModel
import com.example.playlistmaker.settings.data.provider.ResourcesProvider
import com.example.playlistmaker.settings.data.provider.impl.ResourcesProviderImpl
import com.example.playlistmaker.settings.domain.repository.ExternalActionsRepository
import com.example.playlistmaker.settings.domain.repository.SettingsRepository
import com.example.playlistmaker.settings.data.repository.impl.ExternalActionsRepositoryImpl
import com.example.playlistmaker.settings.data.repository.impl.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.settings.domain.interactor.SharingInteractor
import com.example.playlistmaker.settings.domain.interactor.impl.SharingInteractorImpl
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import com.google.gson.Gson
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



private const val ITUNES_BASE_URL = "https://itunes.apple.com"

val networkModule = module {
    single<Gson> { Gson() }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    single<iTunesSearchAPI> {
        get<Retrofit>().create(iTunesSearchAPI::class.java)
    }
}

val dataModule = module {

    factory<MediaPlayer> {
        MediaPlayer()
    }

    single<PlayerRepository> {
        PlayerRepositoryImpl(mediaPlayer = get()) }

    single<TrackRepository> {
        TrackRepositoryImpl(get(),gson = get(),
            favoriteTracksRepository = get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(context = get(),gson = get(),
            favoriteTracksRepository = get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(context = get())
    }

    single<ExternalActionsRepository> {
        ExternalActionsRepositoryImpl(context = get())
    }

    single<ResourcesProvider> {
        ResourcesProviderImpl(context = get())
    }

    single { TrackMapper }

    single<AppDatabase> {
        AppDatabase.getInstance(get())
    }

    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(database = get())
    }

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(database = get())
    }


}

val domainModule = module {


    factory<PlayerInteractor> {
        PlayerInteractorImpl(get())
    }

    factory<SearchInteractor> {
        SearchInteractorImpl(get())
    }

    factory<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    factory<SettingsInteractor> {
        SettingsInteractor(get())
    }

    factory<SharingInteractor> {
        SharingInteractorImpl(
            externalActionsRepository = get(),
            resourcesProvider = get()
        )
    }

    factory<FavoriteTracksInteractor> {
        FavoriteTracksInteractorImpl(get())
    }

    factory<PlaylistsInteractor> {
        PlaylistsInteractorImpl(get())
    }

}

val viewModelModule = module {
    viewModel { PlayerViewModel(
        get(),
        favoriteTracksInteractor = get(),
        playlistsInteractor = get()
    ) }

    viewModel {
        SearchViewModel(
            searchInteractor = get(),
            searchHistoryInteractor = get()
        )
    }

    viewModel {
        SettingsViewModel(
            settingsInteractor = get(),
            sharingInteractor = get()
        )
    }

    viewModel { FavoriteTracksViewModel(favoriteTracksInteractor = get()) }
    viewModel { PlaylistsViewModel(playlistsInteractor = get()) }

}



val appModule = listOf(networkModule, dataModule, domainModule, viewModelModule)