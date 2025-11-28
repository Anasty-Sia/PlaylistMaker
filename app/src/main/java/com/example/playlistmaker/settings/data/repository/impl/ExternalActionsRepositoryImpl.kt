package com.example.playlistmaker.settings.data.repository.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.playlistmaker.settings.domain.model.ShareData
import com.example.playlistmaker.settings.domain.model.SupportData
import com.example.playlistmaker.settings.domain.model.TermsData
import com.example.playlistmaker.settings.domain.repository.ExternalActionsRepository

class ExternalActionsRepositoryImpl(private val context: Context) : ExternalActionsRepository {

    override fun shareApp(shareData: ShareData) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareData.text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooserIntent = Intent.createChooser(shareIntent, shareData.title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }

    override fun openSupport(supportData: SupportData) {
        val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(supportData.email))
            putExtra(Intent.EXTRA_SUBJECT, supportData.subject)
            putExtra(Intent.EXTRA_TEXT, supportData.message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(supportIntent)
    }

    override fun openTerms(termsData: TermsData) {
        val termsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(termsData.url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(termsIntent)
    }
}