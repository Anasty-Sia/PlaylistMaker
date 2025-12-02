package com.example.playlistmaker.settings.domain.repository

import com.example.playlistmaker.settings.domain.model.ShareData
import com.example.playlistmaker.settings.domain.model.SupportData
import com.example.playlistmaker.settings.domain.model.TermsData

interface ExternalActionsRepository {
    fun shareApp(shareData: ShareData)
    fun openSupport(supportData: SupportData)
    fun openTerms(termsData: TermsData)
}