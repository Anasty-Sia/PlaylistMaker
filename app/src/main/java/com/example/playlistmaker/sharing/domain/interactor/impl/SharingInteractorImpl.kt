package com.example.playlistmaker.sharing.domain.interactor.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.playlistmaker.R
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class SharingInteractorImpl(private val context: Context) : SharingInteractor {

    override fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text))
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_title)))
    }

    override fun openSupport() {
        val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.email_support)))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.email_message))
        }
        context.startActivity(supportIntent)
    }

    override fun openTerms() {
        val termsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(context.getString(R.string.terms_of_service_url))
        }
        context.startActivity(termsIntent)
    }
}