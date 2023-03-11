package com.hocel.texty.data.models

import android.net.Uri

data class ScannedText(
    val text: String = "",
    var imageUri: String = "",
    val scannedTime: Long = System.currentTimeMillis(),
    val textLanguage: String = ""
)
