package com.hocel.texty.data.models

data class User(
    var userID: String = "",
    var name: String = "",
    var email: String = "",
    var listOfScannedText: List<ScannedText> = listOf(),
)
