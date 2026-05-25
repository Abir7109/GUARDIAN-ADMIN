package com.securphone.app.data.models

data class SessionModel(
    val id: String = "",
    val userId: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val duration: Long = 0,
    val appVersion: String = ""
)
