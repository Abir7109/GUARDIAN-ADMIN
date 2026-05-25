package com.securphone.app.data.models

data class DeviceInfoModel(
    val model: String = "",
    val manufacturer: String = "",
    val androidVersion: String = "",
    val sdkVersion: Int = 0,
    val osVersion: String = "",
    val screenResolution: String = "",
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val lastLatitude: Double = 0.0,
    val lastLongitude: Double = 0.0,
    val fingerprintRegistered: Boolean = false,
    val shieldActive: Boolean = false,
    val lastHeartbeat: Long = 0,
    val clearanceLevel: Int = 0,
    val deviceAlias: String = ""
)
