package com.securphone.app.data.models

data class UserModel(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val phone: String = "",
    val pinHash: String = "",
    val memberStatus: String = "free",
    val clearanceLevel: Int = 0,
    val isBlocked: Boolean = false,
    val appVersion: String = "",
    val fcmToken: String = "",
    val deviceAlias: String = "",
    val osVersion: String = "",
    val deviceModel: String = "",
    val fingerprintRegistered: Boolean = false,
    val shieldActive: Boolean = false,
    val centralSync: Boolean = false,
    val deviceInfo: DeviceInfoModel = DeviceInfoModel(),
    val settings: UserSettingsModel = UserSettingsModel(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis(),
    val lastSyncTimestamp: Long = 0
)
