package com.securphone.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import android.util.Log
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.models.EventModel
import com.securphone.app.data.models.SystemInfoModel
import com.securphone.app.data.preferences.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object EmergencyNotifier {
    private const val TAG = "EmergencyNotifier"

    fun triggerEmergency(context: Context) {
        Log.w(TAG, "=== EMERGENCY TRIGGERED ===")
        vibrate(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val contacts = PreferencesManager.getEmergencyContacts(context)
                Log.d(TAG, "Emergency contacts found: ${contacts.size}")
                if (contacts.isEmpty()) {
                    Log.w(TAG, "No emergency contacts — nothing to send")
                    return@launch
                }

                val location = LocationHelper.getCurrentLocation(context)
                val systemInfo: SystemInfoModel = SystemInfoHelper.getSystemInfo(context)
                val message = buildEmergencyMessage(location, systemInfo)

                for (contactEntry in contacts) {
                    val parts = contactEntry.split("|", limit = 2)
                    if (parts.size == 2) {
                        val phone = parts[1].trim()
                        val name = parts[0]
                        Log.d(TAG, "Notifying: $name ($phone)")

                        // SMS
                        sendSmsDirect(context, phone, message)
                    }
                }
                Log.w(TAG, "Emergency SMS sent")

                try {
                    val userId = FirebaseManager.getCurrentUser()?.uid
                    if (userId != null) {
                        if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                            FirebaseManager.updateLocation(userId, location.latitude, location.longitude)
                        }
                        FirebaseManager.logEvent(EventModel(
                            userId = userId,
                            type = Constants.EVENT_EMERGENCY_TRIGGERED,
                            triggerSource = "power_off_attempt",
                            latitude = location?.latitude ?: 0.0,
                            longitude = location?.longitude ?: 0.0,
                            timestamp = System.currentTimeMillis()
                        ))
                        FirebaseManager.updateAlarmStatus(userId, true)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase update failed (non-critical)", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Emergency trigger failed completely", e)
            }
        }
    }

    fun sendWhatsApp(context: Context, phone: String, message: String) {
        try {
            val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d(TAG, "WhatsApp opened for $cleanPhone")
        } catch (e: Exception) {
            Log.e(TAG, "WhatsApp not installed or failed", e)
        }
    }

    private fun sendSmsDirect(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            Log.d(TAG, "SMS sent to $phoneNumber")
        } catch (e: SecurityException) {
            Log.e(TAG, "SEND_SMS permission not granted!", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
        }
    }

    private fun vibrate(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }

    private fun buildEmergencyMessage(location: android.location.Location?, systemInfo: SystemInfoModel): String {
        val lat = location?.latitude ?: 0.0
        val lng = location?.longitude ?: 0.0
        return """GUARDIAN EMERGENCY ALERT
Phone: ${systemInfo.model}
Location: https://maps.google.com/?q=$lat,$lng
Battery: ${systemInfo.batteryLevel}%
Network: ${systemInfo.networkType}
Time: ${systemInfo.currentTime}
Power-off attempt detected!"""
    }
}
