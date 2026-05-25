package com.securphone.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.securphone.app.R
import com.securphone.app.utils.AlarmHelper
import com.securphone.app.utils.Constants

class SirenService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var originalVolume = 0
    private var volumeGuardHandler: Handler? = null
    private var volumeGuardThread: HandlerThread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        AlarmHelper.sirenService = this
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION_SILENCE_SIREN) {
            stopSirenInternal()
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        startSiren()

        return START_STICKY
    }

    private fun startSiren() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)

            mediaPlayer = MediaPlayer.create(this, R.raw.siren).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                start()
            }

            startVolumeGuard()
        } catch (e: Exception) {
            android.util.Log.e("SirenService", "Failed to start siren", e)
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 1000, 500, 1000, 500, 1000), 0
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000), 0)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        if (AlarmHelper.sirenService === this) {
            AlarmHelper.sirenService = null
        }
        stopSirenInternal()
        try { stopForeground(true) } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun startVolumeGuard() {
        volumeGuardThread = HandlerThread("SirenVolumeGuard").apply { start() }
        volumeGuardHandler = Handler(volumeGuardThread!!.looper)
        volumeGuardHandler?.post(object : Runnable {
            override fun run() {
                try {
                    val am = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    am?.let {
                        val max = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        if (it.getStreamVolume(AudioManager.STREAM_MUSIC) < max) {
                            it.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0)
                        }
                    }
                } catch (_: Exception) {}
                if (mediaPlayer != null) {
                    volumeGuardHandler?.postDelayed(this, 300)
                }
            }
        })
    }

    private fun stopVolumeGuard() {
        volumeGuardHandler?.removeCallbacksAndMessages(null)
        volumeGuardHandler = null
        volumeGuardThread?.quitSafely()
        volumeGuardThread = null
    }

    fun stopSirenNow() {
        stopSirenInternal()
        try { stopForeground(true) } catch (_: Exception) {}
        stopSelf()
    }

    private fun stopSirenInternal() {
        stopVolumeGuard()

        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        } catch (_: Exception) {}

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (_: Exception) {}

        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Siren Service", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Plays alarm siren in background"
                setSound(null, null)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("Siren Active")
            .setContentText("Alarm is playing")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "sp_siren_channel"
        private const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            val intent = Intent(context, SirenService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
