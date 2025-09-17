package com.example.stb_scanner.model

import android.net.Uri

data class TvChannel(
    val id: Long,
    val name: String?,
    val number: String?,
    val logoUri: Uri?,
    val inputId: String
)
