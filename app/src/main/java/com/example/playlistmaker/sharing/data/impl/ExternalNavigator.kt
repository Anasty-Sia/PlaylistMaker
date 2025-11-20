package com.example.playlistmaker.sharing.data.impl

import com.example.playlistmaker.sharing.domain.model.EmailData

interface ExternalNavigator {
    fun openEmail(emailData: EmailData)
    fun openUrl(url: String)
    fun shareText(text: String, title: String)
}