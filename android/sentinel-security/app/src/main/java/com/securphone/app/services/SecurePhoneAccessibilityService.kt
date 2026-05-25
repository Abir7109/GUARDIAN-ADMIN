package com.securphone.app.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.ui.alarm.AlarmOverlayActivity
import com.securphone.app.ui.lock.GuardLockActivity
import com.securphone.app.utils.AlarmHelper
import com.securphone.app.utils.Constants
import com.securphone.app.utils.EmergencyNotifier

class SecurePhoneAccessibilityService : AccessibilityService() {

    private var volumeUpCounter = 0
    private val mainHandler = Handler(Looper.getMainLooper())
    private val volumeUpReset = Runnable { volumeUpCounter = 0 }

    private val oemPowerPackages = hashSetOf(
        "com.samsung.android.globalactions"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo?.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        val isPowerDialog = (oemPowerPackages.contains(packageName) ||
            className.contains("globalactions", ignoreCase = true) ||
            className.contains("powerdialog", ignoreCase = true) ||
            className.contains("power_menu", ignoreCase = true))

        if (!isPowerDialog) return

        Log.w("A11yWatcher", "Detected power dialog — package: $packageName class: $className")

        if (AlarmHelper.isSirenActive) {
            // During alarm — dismiss power menu and re-assert overlay
            collapseSystemDialogs()
            return
        }

        if (PreferencesManager.isProtectionActive(this)) {
            Log.w("A11yWatcher", "Power-off attempt while protected — EMERGENCY!")
            val prefs = PreferencesManager.getEmergencyContacts(this)
            Log.w("A11yWatcher", "Emergency contacts count: ${prefs.size}")

            // SMS + Firebase via coroutine (primary channel — actually sends)
            EmergencyNotifier.triggerEmergency(this)

            // Lock the phone
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            val lockIntent = Intent(this, GuardLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(lockIntent)
        } else {
            Log.d("A11yWatcher", "Power dialog ignored — protection not active")
        }
    }

    private fun collapseSystemDialogs() {
        performGlobalAction(GLOBAL_ACTION_BACK)
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        mainHandler.postDelayed({ reassertAlarmOverlay() }, 150)
    }

    private fun reassertAlarmOverlay() {
        val intent = Intent(this, AlarmOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("A11yWatcher", "reassertAlarmOverlay failed", e)
        }
    }

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false
        if (!AlarmHelper.isSirenActive) return super.onKeyEvent(event)

        when (event.keyCode) {
            KeyEvent.KEYCODE_POWER -> {
                when (event.action) {
                    KeyEvent.ACTION_DOWN -> {
                        if (event.repeatCount == 0) {
                            volumeUpCounter = 0
                            mainHandler.removeCallbacks(volumeUpReset)
                        }
                    }
                }
                return true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    sendBroadcast(Intent(Constants.ACTION_ALARM_SHAKE))
                }
                return true
            }

            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    volumeUpCounter++
                    mainHandler.removeCallbacks(volumeUpReset)
                    mainHandler.postDelayed(volumeUpReset, 3000L)
                    if (volumeUpCounter >= 3) {
                        volumeUpCounter = 0
                        mainHandler.removeCallbacks(volumeUpReset)
                        AlarmHelper.stopSiren()
                        val i = Intent(this, AlarmOverlayActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(i)
                    }
                }
                return false
            }
        }
        return super.onKeyEvent(event)
    }
}
