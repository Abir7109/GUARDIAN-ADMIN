package com.securphone.app.data.models

data class AuditLogEntry(
    val id: String = "",
    val userId: String = "",
    val action: String = "",
    val category: String = "access",
    val description: String = "",
    val metadata: Map<String, Any> = emptyMap(),
    val ipAddress: String = "",
    val userAgent: String = "",
    val resolved: Boolean = false,
    val resolutionNotes: List<String> = emptyList(),
    val assignedAdmin: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
