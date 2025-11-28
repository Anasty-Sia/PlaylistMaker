package com.example.playlistmaker.settings.domain.interactor.impl


import com.example.playlistmaker.R
import com.example.playlistmaker.settings.data.provider.ResourcesProvider
import com.example.playlistmaker.settings.domain.repository.ExternalActionsRepository
import com.example.playlistmaker.settings.domain.interactor.SharingInteractor
import com.example.playlistmaker.settings.domain.model.ShareData
import com.example.playlistmaker.settings.domain.model.SupportData
import com.example.playlistmaker.settings.domain.model.TermsData


class SharingInteractorImpl(
    private val externalActionsRepository: ExternalActionsRepository,
    private val resourcesProvider: ResourcesProvider
) : SharingInteractor {

    override fun getShareData(): ShareData {
        return ShareData(
            text = resourcesProvider.getString(R.string.share_text),
            title = resourcesProvider.getString(R.string.share_title)
        )
    }

    override fun getSupportData(): SupportData {
        return SupportData(
            email = resourcesProvider.getString(R.string.email_support),
            subject = resourcesProvider.getString(R.string.email_subject),
            message = resourcesProvider.getString(R.string.email_message)
        )
    }

    override fun getTermsData(): TermsData {
        return TermsData(
            url = resourcesProvider.getString(R.string.terms_of_service_url)
        )
    }

    override fun shareApp() {
        externalActionsRepository.shareApp(getShareData())
    }

    override fun openSupport() {
        externalActionsRepository.openSupport(getSupportData())
    }

    override fun openTerms() {
        externalActionsRepository.openTerms(getTermsData())
    }
}
