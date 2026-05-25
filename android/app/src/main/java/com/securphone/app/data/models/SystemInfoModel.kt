package com.securphone.app.data.models

data class SystemInfoModel(
    val model: String = "",
    val androidVersion: String = "",
    val sdkVersion: Int = 0,
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val networkType: String = "",
    val wifiName: String? = null,
    val screenBrightness: Int = 0,
    val currentTime: String = "",
    val uptime: Long = 0L,
    val availableStorage: Long = 0L,
    val totalStorage: Long = 0L
)
