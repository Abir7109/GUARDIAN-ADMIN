package com.securphone.app.data.models

data class BroadcastModel(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val audience: String = "global",
    val actionUri: String = "",
    val scheduledTime: Long = 0,
    val deliveredCount: Int = 0,
    val successPercentage: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "pending"
)
