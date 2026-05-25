package com.securphone.app.data.models

data class AdminModel(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "operator",
    val permissions: List<String> = emptyList(),
    val lastActive: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
