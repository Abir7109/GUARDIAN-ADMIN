package com.securphone.app.data.models

data class AnalyticsSnapshotModel(
    val id: String = "",
    val date: String = "",
    val dailyActiveUsers: Int = 0,
    val monthlyActiveUsers: Int = 0,
    val dauMauRatio: Double = 0.0,
    val enrolledAlphaCount: Int = 0,
    val enrolledBetaCount: Int = 0,
    val enrolledGammaCount: Int = 0,
    val enrolledLegacyCount: Int = 0,
    val onboardingEnrolled: Int = 0,
    val onboardingFingerprints: Int = 0,
    val onboardingShieldActive: Int = 0,
    val onboardingCentralSync: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
