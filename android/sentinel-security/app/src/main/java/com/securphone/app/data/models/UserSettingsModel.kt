package com.securphone.app.data.models

data class UserSettingsModel(
    val isProtectionActive: Boolean = false,
    val triggerKeyword: String = "Where are you",
    val alarmVolume: Int = 100,
    val sirenType: String = "default",
    val trustedContacts: List<String> = emptyList()
)
