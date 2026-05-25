package com.securphone.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.models.EventModel
import com.securphone.app.data.models.SystemInfoModel
import com.securphone.app.data.preferences.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

enum class TriggerSource { SMS, WHATSAPP, MANUAL }

object TriggerManager {
    private val lastTriggerTime = AtomicLong(0L)

    private fun isInSafeZone(context: Context, location: Location?): Boolean {
        if (location == null) return false
        if (!PreferencesManager.isSafeLocationEnabled(context)) return false
        val safeLat = PreferencesManager.getSafeZoneLat(context)
        val safeLng = PreferencesManager.getSafeZoneLng(context)
        val radius = PreferencesManager.getSafeZoneRadius(context)
        if (safeLat == 0.0 && safeLng == 0.0) return false

        val results = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude, safeLat, safeLng, results)
        val distance = results[0]
        Log.d("TriggerManager", "Distance from safe zone: ${distance}m (radius: ${radius}m)")
        return distance <= radius.toFloat()
    }

    fun triggerBeast(context: Context, source: TriggerSource = TriggerSource.SMS, senderNumber: String? = null, replyAction: NotificationReplyAction? = null) {
        val now = System.currentTimeMillis()
        if (source != TriggerSource.MANUAL) {
            val prev = lastTriggerTime.get()
            if (now - prev < Constants.TRIGGER_COOLDOWN_MS) return
            if (!lastTriggerTime.compareAndSet(prev, now)) return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = FirebaseManager.getCurrentUser()?.uid ?: return@launch

                val location: Location? = LocationHelper.getCurrentLocation(context)

                // Check safe zone — suppress alarm if in safe zone
                if (isInSafeZone(context, location)) {
                    Log.w("TriggerManager", "Device is in safe zone — suppressing alarm")
                    return@launch
                }

                FirebaseManager.logEvent(EventModel(
                    userId = userId,
                    type = Constants.EVENT_TRIGGER_ACTIVATED,
                    triggerSource = source.name.lowercase(),
                    triggerNumber = senderNumber ?: "",
                    timestamp = System.currentTimeMillis()
                ))

                Log.d("TriggerManager", "getCurrentLocation returned: ${location?.latitude},${location?.longitude}")
                val systemInfo: SystemInfoModel = SystemInfoHelper.getSystemInfo(context)

                val responseMessage = buildResponseMessage(location, systemInfo)

                // Send reply via SMS (if sender number is available)
                if (senderNumber != null && senderNumber.matches(Regex("^\\+?[0-9]{7,15}$"))) {
                    SmsSender.sendSms(context, senderNumber, responseMessage)
                }

                // Send reply via notification RemoteInput (WhatsApp/Messenger)
                if (replyAction != null) {
                    Log.d("TriggerManager", "Firing RemoteInput reply via notification action")
                    NotificationReplier.sendReply(context, replyAction, responseMessage)
                }

                if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                    val result = FirebaseManager.updateLocation(userId, location.latitude, location.longitude)
                    Log.d("TriggerManager", "updateLocation result: $result")
                } else {
                    Log.w("TriggerManager", "Skipping location update — invalid coords: ${location?.latitude},${location?.longitude}")
                }

                AlarmHelper.startSiren(context)
                FirebaseManager.updateAlarmStatus(userId, true)

                try {
                    val alarmIntent = Intent(context, com.securphone.app.ui.alarm.AlarmOverlayActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    val pi = PendingIntent.getActivity(context, 1001, alarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 200, pi)
                } catch (e: Exception) {
                    try {
                        val alarmIntent = Intent(context, com.securphone.app.ui.alarm.AlarmOverlayActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        context.startActivity(alarmIntent)
                    } catch (_: Exception) {}
                }

                FirebaseManager.logEvent(EventModel(
                    userId = userId,
                    type = Constants.EVENT_ALARM_STARTED,
                    triggerSource = source.name.lowercase(),
                    latitude = location?.latitude ?: 0.0,
                    longitude = location?.longitude ?: 0.0,
                    timestamp = System.currentTimeMillis()
                ))
            } catch (e: Exception) {
                android.util.Log.e("TriggerManager", "triggerBeast crashed", e)
            }
        }
    }

    private fun buildResponseMessage(location: Location?, systemInfo: SystemInfoModel): String {
        val lat = location?.latitude ?: 0.0
        val lng = location?.longitude ?: 0.0
        return """
            SECURPHONE ALERT
            Location: https://maps.google.com/?q=$lat,$lng
            Device: ${systemInfo.model}
            Battery: ${systemInfo.batteryLevel}%
            Network: ${systemInfo.networkType}
            Time: ${systemInfo.currentTime}
            Alarm has been activated!
        """.trimIndent()
    }
}
