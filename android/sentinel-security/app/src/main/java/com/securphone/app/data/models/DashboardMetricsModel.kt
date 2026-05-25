package com.securphone.app.data.models

data class DashboardMetricsModel(
    val totalRegisteredDevices: Int = 0,
    val activeDevicesToday: Int = 0,
    val threatTriggersActivated: Int = 0,
    val guardedFleetShield: Double = 0.0,
    val operationsVolume: List<Long> = emptyList(),
    val criticalThreatPoints: Double = 0.0,
    val hardwareAlarms: Double = 0.0,
    val authorizedSessions: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
