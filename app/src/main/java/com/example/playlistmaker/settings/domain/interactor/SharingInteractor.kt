package com.example.playlistmaker.settings.domain.interactor

import com.example.playlistmaker.settings.domain.model.ShareData
import com.example.playlistmaker.settings.domain.model.SupportData
import com.example.playlistmaker.settings.domain.model.TermsData

interface SharingInteractor {
    fun getShareData(): ShareData
    fun getSupportData(): SupportData
    fun getTermsData(): TermsData
    fun shareApp()
    fun openSupport()
    fun openTerms()
}