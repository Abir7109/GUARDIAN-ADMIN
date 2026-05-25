package com.securphone.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.AlarmManager
import com.securphone.app.data.NotificationStore
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.models.BroadcastModel
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.ui.alarm.AlarmOverlayActivity
import com.securphone.app.ui.auth.LoginActivity
import com.securphone.app.ui.lock.GuardLockActivity
import com.securphone.app.ui.main.MainActivity
import com.securphone.app.ui.maintenance.MaintenanceActivity
import com.securphone.app.ui.update.ForceUpdateActivity
import com.securphone.app.utils.AlarmHelper
import com.securphone.app.utils.Constants
import com.securphone.app.utils.TriggerManager
import com.securphone.app.utils.TriggerSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SecurePhoneMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            val userId = FirebaseManager.getCurrentUser()?.uid ?: return@launch
            FirebaseManager.updateFcmToken(userId, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val action = remoteMessage.data["action"] ?: return
        val message = remoteMessage.data["message"] ?: ""
        val active = remoteMessage.data["active"]

        when (action) {
            Constants.ADMIN_ACTION_FORCE_UPDATE -> {
                val intent = Intent(this, ForceUpdateActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            }
            Constants.ADMIN_ACTION_BLOCK_USER -> {
                PreferencesManager.clearAll(this)
                FirebaseManager.signOut()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            }
            Constants.ADMIN_ACTION_REMOTE_TRIGGER -> {
                val senderNumber = remoteMessage.data["sender"] ?: ""
                TriggerManager.triggerBeast(
                    this,
                    source = TriggerSource.MANUAL,
                    senderNumber = senderNumber
                )
            }
            Constants.ADMIN_ACTION_LOCK -> {
                try {
                    val intent = Intent(this, AlarmOverlayActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    val pi = PendingIntent.getActivity(this, 1002, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    val am = getSystemService(ALARM_SERVICE) as AlarmManager
                    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 200, pi)
                } catch (_: Exception) {}
            }
            Constants.ADMIN_ACTION_KILL_CONNECTION -> {
                PreferencesManager.clearAll(this)
                val intent = Intent(this, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            }
            Constants.ADMIN_ACTION_KEY_SYNC -> {
                serviceScope.launch {
                    val userId = FirebaseManager.getCurrentUser()?.uid ?: return@launch
                    FirebaseManager.syncDevice(userId)
                }
            }
            Constants.ADMIN_ACTION_TOGGLE_SHIELD -> {
                val isActive = active.toBoolean()
                serviceScope.launch {
                    val userId = FirebaseManager.getCurrentUser()?.uid ?: return@launch
                    FirebaseManager.toggleShield(userId, isActive)
                    PreferencesManager.setProtectionActive(this@SecurePhoneMessagingService, isActive)
                    if (isActive) {
                        try {
                            val lockIntent = Intent(this@SecurePhoneMessagingService, GuardLockActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            val pi = PendingIntent.getActivity(this@SecurePhoneMessagingService, 1003, lockIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                            val am = getSystemService(ALARM_SERVICE) as AlarmManager
                            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 300, pi)
                        } catch (_: Exception) {}
                    }
                }
            }
            Constants.ADMIN_ACTION_UPDATE_POLICY -> {
                serviceScope.launch {
                    FirebaseManager.fetchRemoteConfig()
                    FirebaseManager.getGlobalPolicyConfig(this@SecurePhoneMessagingService)
                    val ctx = this@SecurePhoneMessagingService
                    if (PreferencesManager.isMaintenanceMode(ctx)) {
                        val intent = Intent(ctx, MaintenanceActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        launch(Dispatchers.Main) { startActivity(intent) }
                    } else if (PreferencesManager.isForceUpdateEnabled(ctx)) {
                        val minVer = PreferencesManager.getMinRequiredVersion(ctx)
                        if (minVer.isNotBlank() && isVersionLowerThan(Constants.CURRENT_VERSION, minVer)) {
                            val intent = Intent(ctx, ForceUpdateActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            launch(Dispatchers.Main) { startActivity(intent) }
                        }
                    }
                }
            }
            Constants.ADMIN_ACTION_CLEARANCE_UPDATE -> {
                val level = remoteMessage.data["level"]?.toIntOrNull() ?: return
                serviceScope.launch {
                    val userId = FirebaseManager.getCurrentUser()?.uid ?: return@launch
                    FirebaseManager.updateClearance(userId, level)
                }
            }
            Constants.ADMIN_ACTION_SEND_BROADCAST -> {
                val title = remoteMessage.data["title"] ?: return
                val body = remoteMessage.data["body"] ?: ""
                val broadcast = BroadcastModel(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    body = body,
                    createdAt = System.currentTimeMillis()
                )
                NotificationStore.add(broadcast)
                showBroadcastNotification(title, body)
            }
            Constants.ADMIN_ACTION_STOP_ALARM -> {
                AlarmHelper.stopSiren()
                sendBroadcast(Intent(Constants.ACTION_ALARM_STOPPED))
                serviceScope.launch {
                    val userId = FirebaseManager.getCurrentUser()?.uid ?: return@launch
                    FirebaseManager.updateAlarmStatus(userId, false)
                }
            }
        }
    }

    private fun showBroadcastNotification(title: String, body: String) {
        val channelId = Constants.CHANNEL_ALERTS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Admin Broadcasts", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Broadcasts from admin panel" }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.securphone.app.R.drawable.ic_shield)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun isVersionLowerThan(current: String, minimum: String): Boolean {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val minParts = minimum.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(currentParts.size, minParts.size)) {
            val c = currentParts.getOrElse(i) { 0 }
            val m = minParts.getOrElse(i) { 0 }
            if (c < m) return true
            if (c > m) return false
        }
        return false
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
