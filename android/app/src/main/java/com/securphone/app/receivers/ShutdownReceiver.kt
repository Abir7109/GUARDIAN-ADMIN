package com.securphone.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.models.EventModel
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.utils.AlarmHelper
import com.securphone.app.utils.Constants
import com.securphone.app.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class ShutdownReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_SHUTDOWN && action != "android.intent.action.QUICKBOOT_POWEROFF") return
        if (!PreferencesManager.isProtectionActive(context)) return

        Log.w("ShutdownReceiver", "Device shutting down while protection is active!")

        runBlocking {
            try {
                withTimeout(5000) {
                    val userId = FirebaseManager.getCurrentUser()?.uid ?: return@withTimeout

                    val location = withContext(Dispatchers.IO) {
                        LocationHelper.getCurrentLocation(context)
                    }
                    if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                        FirebaseManager.updateLocation(userId, location.latitude, location.longitude)
                        Log.d("ShutdownReceiver", "Final location sent: ${location.latitude},${location.longitude}")
                    }

                    FirebaseManager.logEvent(EventModel(
                        userId = userId,
                        type = Constants.EVENT_ADMIN_DISABLED,
                        triggerSource = "shutdown",
                        timestamp = System.currentTimeMillis()
                    ))
                }
            } catch (e: Exception) {
                Log.e("ShutdownReceiver", "Failed to send final data before shutdown", e)
            }
        }

        AlarmHelper.stopSiren()
    }
}
