package com.securphone.app.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

enum class PermissionType {
    NOTIFICATION_LISTENER,
    LOCATION,
    BACKGROUND_LOCATION,
    OVERLAY,
    ACCESSIBILITY,
    BATTERY_OPTIMIZATION,
    SMS
}

class PermissionManager(private val context: Context) {

    fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabledListeners.contains(context.packageName)
    }

    fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun isBackgroundLocationGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun isOverlayPermissionGranted(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val serviceName = "${context.packageName}/.services.SecurePhoneAccessibilityService"
        return enabledServices.contains(serviceName)
    }

    fun isBatteryOptimizationIgnored(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isSmsPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    fun getNotificationListenerSettingsIntent(): Intent {
        return Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
    }

    fun getOverlaySettingsIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
    }

    fun getAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    fun getBatteryOptimizationIntent(): Intent {
        return Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            android.net.Uri.parse("package:${context.packageName}")
        )
    }

    fun areAllPermissionsGranted(): Boolean {
        return isNotificationListenerEnabled() &&
                isLocationPermissionGranted() &&
                isBackgroundLocationGranted() &&
                isOverlayPermissionGranted() &&
                isAccessibilityServiceEnabled() &&
                isBatteryOptimizationIgnored() &&
                isSmsPermissionGranted()
    }

    fun getMissingPermissions(): List<PermissionType> {
        val missing = mutableListOf<PermissionType>()
        if (!isNotificationListenerEnabled()) missing.add(PermissionType.NOTIFICATION_LISTENER)
        if (!isLocationPermissionGranted()) missing.add(PermissionType.LOCATION)
        if (!isBackgroundLocationGranted()) missing.add(PermissionType.BACKGROUND_LOCATION)
        if (!isOverlayPermissionGranted()) missing.add(PermissionType.OVERLAY)
        if (!isAccessibilityServiceEnabled()) missing.add(PermissionType.ACCESSIBILITY)
        if (!isBatteryOptimizationIgnored()) missing.add(PermissionType.BATTERY_OPTIMIZATION)
        if (!isSmsPermissionGranted()) missing.add(PermissionType.SMS)
        return missing
    }
}
