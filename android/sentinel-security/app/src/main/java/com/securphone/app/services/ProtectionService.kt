package com.securphone.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.securphone.app.R
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.ui.lock.GuardLockActivity
import com.securphone.app.utils.Constants
import com.securphone.app.utils.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProtectionService : Service() {

    companion object {
        const val ACTION_START = "com.securphone.app.action.START_PROTECTION"
        const val ACTION_STOP = "com.securphone.app.action.STOP_PROTECTION"
        private const val LOCATION_INTERVAL_MS = 300000L
    }

    private val CHANNEL_ID = Constants.CHANNEL_PROTECTION
    private var powerReceiver: BroadcastReceiver? = null
    private var usbReceiver: BroadcastReceiver? = null
    private var simReceiver: BroadcastReceiver? = null
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var wasProximityNear = false
    private var locationHandler: Handler? = null
    private val locationRunnable = Runnable { trackLocation() }

    private val proximityListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!PreferencesManager.isProtectionActive(this@ProtectionService)) return
            if (!PreferencesManager.isPocketShieldEnabled(this@ProtectionService)) return
            val distance = event.values[0]
            val maxRange = proximitySensor?.maximumRange ?: return
            val isNear = distance < maxRange * 0.5f
            if (wasProximityNear && !isNear) {
                triggerAlarm()
            }
            wasProximityNear = isNear
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerPowerReceiver()
        registerUsbReceiver()
        registerSimReceiver()
        registerProximitySensor()
        startLocationTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                val notification = createNotification()
                startForeground(1, notification)
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartIntent = Intent(this, ProtectionService::class.java).apply {
            action = ACTION_START
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent)
            } else {
                startService(restartIntent)
            }
        } catch (_: Exception) {
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Guardian active")
            .setContentText("Actively monitoring device environment sensors.")
            .setSmallIcon(R.drawable.ic_shield)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Guardian — Anti-Theft Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun registerPowerReceiver() {
        powerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || context == null) return
                if (!PreferencesManager.isProtectionActive(context)) return
                if (intent.action == Intent.ACTION_POWER_DISCONNECTED) {
                    if (PreferencesManager.isChargerUnplugEnabled(context)) {
                        triggerAlarm()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_POWER_CONNECTED)
        }
        registerReceiver(powerReceiver, filter, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_EXPORTED else 0)
    }

    private fun registerUsbReceiver() {
        usbReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || context == null) return
                if (!PreferencesManager.isProtectionActive(context)) return
                if (intent.action == "android.hardware.usb.action.USB_STATE") {
                    val connected = intent.getBooleanExtra("connected", false)
                    if (connected && PreferencesManager.isUsbBlockEnabled(context)) {
                        triggerAlarm()
                    }
                }
            }
        }
        val filter = IntentFilter("android.hardware.usb.action.USB_STATE")
        registerReceiver(usbReceiver, filter, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_EXPORTED else 0)
    }

    private fun registerSimReceiver() {
        simReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || context == null) return
                if (!PreferencesManager.isProtectionActive(context)) return
                if (intent.action == "android.intent.action.SIM_STATE_CHANGED") {
                    val state = intent.getStringExtra("ss") ?: return
                    if (state == "ABSENT" || state == "NOT_READY") {
                        if (PreferencesManager.isSimAlertEnabled(context)) {
                            triggerAlarm()
                        }
                    }
                }
            }
        }
        val filter = IntentFilter("android.intent.action.SIM_STATE_CHANGED")
        registerReceiver(simReceiver, filter, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_EXPORTED else 0)
    }

    private fun registerProximitySensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (proximitySensor != null) {
            sensorManager?.registerListener(
                proximityListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun triggerAlarm() {
        val lockIntent = Intent(this, GuardLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(lockIntent)
    }

    private fun startLocationTracking() {
        locationHandler = Handler(Looper.getMainLooper())
        locationHandler?.postDelayed(locationRunnable, LOCATION_INTERVAL_MS)
    }

    private fun stopLocationTracking() {
        locationHandler?.removeCallbacks(locationRunnable)
        locationHandler = null
    }

    private fun trackLocation() {
        if (!PreferencesManager.isProtectionActive(this)) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val location = LocationHelper.getCurrentLocation(this@ProtectionService)
                if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                    FirebaseManager.getCurrentUser()?.uid?.let { uid ->
                        FirebaseManager.updateLocation(uid, location.latitude, location.longitude)
                    }
                }
            } catch (_: Exception) {}
        }
        locationHandler?.postDelayed(locationRunnable, LOCATION_INTERVAL_MS)
    }

    override fun onDestroy() {
        stopLocationTracking()
        powerReceiver?.let { unregisterReceiver(it) }
        usbReceiver?.let { unregisterReceiver(it) }
        simReceiver?.let { unregisterReceiver(it) }
        sensorManager?.unregisterListener(proximityListener)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
