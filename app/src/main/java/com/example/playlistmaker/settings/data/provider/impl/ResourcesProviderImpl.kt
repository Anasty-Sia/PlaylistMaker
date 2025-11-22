package com.example.playlistmaker.settings.data.provider.impl

import android.content.Context
import com.example.playlistmaker.settings.data.provider.ResourcesProvider
class ResourcesProviderImpl(private val context: Context) : ResourcesProvider {
    override fun getString(resId: Int): String = context.getString(resId)
}