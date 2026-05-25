package com.securphone.app.data.models

data class EventModel(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val severity: String = "info",
    val triggerSource: String = "",
    val triggerNumber: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val deviceInfo: Map<String, Any> = emptyMap(),
    val metadata: Map<String, Any> = emptyMap(),
    val resolved: Boolean = false,
    val assignedTo: String = "",
    val notes: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
