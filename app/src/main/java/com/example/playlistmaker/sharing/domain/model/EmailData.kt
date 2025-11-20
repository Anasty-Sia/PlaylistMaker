package com.example.playlistmaker.sharing.domain.model

data class EmailData(
    val emailTo: String,
    val subject: String,
    val message: String
)