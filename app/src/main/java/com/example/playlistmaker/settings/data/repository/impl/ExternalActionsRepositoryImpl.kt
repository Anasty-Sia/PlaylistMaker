package com.example.playlistmaker.settings.data.repository.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.playlistmaker.settings.domain.model.ShareData
import com.example.playlistmaker.settings.domain.model.SupportData
import com.example.playlistmaker.settings.domain.model.TermsData
import com.example.playlistmaker.settings.data.repository.ExternalActionsRepository

class ExternalActionsRepositoryImpl(private val context: Context) : ExternalActionsRepository {

    override fun shareApp(shareData: ShareData) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareData.text)
        }
        context.startActivity(Intent.createChooser(shareIntent, shareData.title))
    }

    override fun openSupport(supportData: SupportData) {
        val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(supportData.email))
            putExtra(Intent.EXTRA_SUBJECT, supportData.subject)
            putExtra(Intent.EXTRA_TEXT, supportData.message)
        }
        context.startActivity(supportIntent)
    }

    override fun openTerms(termsData: TermsData) {
        val termsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(termsData.url)
        }
        context.startActivity(termsIntent)
    }
}