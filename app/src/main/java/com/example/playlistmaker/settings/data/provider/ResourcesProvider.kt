package com.example.playlistmaker.settings.data.provider

interface ResourcesProvider {
    fun getString(resId: Int): String
}