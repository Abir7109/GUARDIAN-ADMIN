package com.securphone.app.ui.alarm

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.securphone.app.R
import com.securphone.app.databinding.OverlayAlarmBinding
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.utils.AlarmHelper
import com.securphone.app.utils.Constants
import com.securphone.app.utils.LocationHelper
import com.securphone.app.utils.PinManager
import com.securphone.app.utils.shake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmOverlayActivity : AppCompatActivity() {

    private lateinit var binding: OverlayAlarmBinding
    private val enteredPin = StringBuilder()
    private var attemptCount = 0
    private var lockoutUntil = 0L
    private var receiverRegistered = false
    private var lockTaskActive = false

    private val focusHandler = Handler(Looper.getMainLooper())
    private val reassertRunnable = Runnable { bringToFrontAndLock() }

    private var hiddenVolumeUpCounter = 0
    private val hiddenVolumeUpHandler = Handler(Looper.getMainLooper())
    private val hiddenVolumeUpReset = Runnable { hiddenVolumeUpCounter = 0 }

    private val alarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.ACTION_ALARM_SHAKE -> {
                    binding.cvAlertHeader.shake()
                    Toast.makeText(this@AlarmOverlayActivity, "DON'T DARE TO SHUTDOWN!", Toast.LENGTH_SHORT).show()
                }
                Constants.ACTION_ALARM_STOPPED -> {
                    if (lockTaskActive) {
                        try { stopLockTask() } catch (_: Exception) {}
                        lockTaskActive = false
                    }
                    AlarmHelper.stopSiren()
                    focusHandler.removeCallbacks(reassertRunnable)
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        binding = OverlayAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.let {
            it.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                it.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            it.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    hideSystemUI()
                }
            }
            hideSystemUI()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                km.requestDismissKeyguard(this, null)
            } catch (_: Exception) {}
        }

        bringToFrontAndLock()
        AlarmHelper.startSiren(this)
        setupKeypadListeners()

        binding.btnDeactivate.setOnClickListener {
            verifyAndUnlock()
        }

        val filter = IntentFilter().apply {
            addAction(Constants.ACTION_ALARM_SHAKE)
            addAction(Constants.ACTION_ALARM_STOPPED)
        }
        ContextCompat.registerReceiver(this, alarmReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        receiverRegistered = true

        fetchLocationAndUpdate()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        } else if (AlarmHelper.isSirenActive) {
            Log.d("AlarmOverlay", "Focus lost — re-asserting in 50ms")
            focusHandler.removeCallbacks(reassertRunnable)
            focusHandler.postDelayed(reassertRunnable, 50)
        }
    }

    private fun bringToFrontAndLock() {
        if (!AlarmHelper.isSirenActive) return
        try {
            startLockTask()
            lockTaskActive = true
        } catch (_: Exception) {
            lockTaskActive = false
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (AlarmHelper.isSirenActive) {
            bringToFrontAndLock()
        }
    }

    override fun onPause() {
        super.onPause()
        if (AlarmHelper.isSirenActive && !isFinishing) {
            focusHandler.postDelayed(reassertRunnable, 50)
        }
    }

    override fun onStop() {
        super.onStop()
        if (AlarmHelper.isSirenActive && !isFinishing) {
            focusHandler.postDelayed(reassertRunnable, 50)
        }
    }

    private fun hideSystemUI() {
        window?.decorView?.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }

    private fun setupKeypadListeners() {
        binding.btnKey1.setOnClickListener { appendPinDigit("1") }
        binding.btnKey2.setOnClickListener { appendPinDigit("2") }
        binding.btnKey3.setOnClickListener { appendPinDigit("3") }
        binding.btnKey4.setOnClickListener { appendPinDigit("4") }
        binding.btnKey5.setOnClickListener { appendPinDigit("5") }
        binding.btnKey6.setOnClickListener { appendPinDigit("6") }
        binding.btnKey7.setOnClickListener { appendPinDigit("7") }
        binding.btnKey8.setOnClickListener { appendPinDigit("8") }
        binding.btnKey9.setOnClickListener { appendPinDigit("9") }
        binding.btnKey0.setOnClickListener { appendPinDigit("0") }
        binding.btnKeyDel.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin.deleteCharAt(enteredPin.length - 1)
                updatePinDots()
            }
        }
    }

    private fun appendPinDigit(digit: String) {
        val now = System.currentTimeMillis()
        if (now < lockoutUntil) {
            val remaining = (lockoutUntil - now) / 1000
            binding.cvAlertHeader.shake()
            Toast.makeText(this, "Locked for ${remaining}s", Toast.LENGTH_SHORT).show()
            return
        }
        if (enteredPin.length < 4) {
            enteredPin.append(digit)
            updatePinDots()
            if (enteredPin.length == 4) {
                verifyAndUnlock()
            }
        }
    }

    private fun updatePinDots() {
        val count = enteredPin.length
        binding.dot1.setBackgroundResource(if (count >= 1) R.drawable.ic_shield else R.drawable.bg_pin_input)
        binding.dot2.setBackgroundResource(if (count >= 2) R.drawable.ic_shield else R.drawable.bg_pin_input)
        binding.dot3.setBackgroundResource(if (count >= 3) R.drawable.ic_shield else R.drawable.bg_pin_input)
        binding.dot4.setBackgroundResource(if (count >= 4) R.drawable.ic_shield else R.drawable.bg_pin_input)
    }

    private fun verifyAndUnlock() {
        val pin = enteredPin.toString()
        attemptCount++

        if (PinManager.verifyPin(this, pin)) {
            if (lockTaskActive) {
                try { stopLockTask() } catch (_: Exception) {}
                lockTaskActive = false
            }
            AlarmHelper.stopSiren()
            CoroutineScope(Dispatchers.IO).launch {
                FirebaseManager.getCurrentUser()?.uid?.let { uid ->
                    FirebaseManager.updateAlarmStatus(uid, false)
                }
            }
            Toast.makeText(this, "Deactivation Successful", Toast.LENGTH_SHORT).show()
            focusHandler.removeCallbacks(reassertRunnable)
            finish()
        } else {
            binding.cvAlertHeader.shake()
            enteredPin.clear()
            updatePinDots()

            if (attemptCount >= Constants.MAX_PIN_ATTEMPTS) {
                lockoutUntil = System.currentTimeMillis() + 30_000L
                attemptCount = 0
                Toast.makeText(this, "Too many attempts — locked for 30s", Toast.LENGTH_LONG).show()
                focusHandler.postDelayed({
                    lockoutUntil = 0L
                    Toast.makeText(this, "You can try PIN again", Toast.LENGTH_SHORT).show()
                }, 30_000L)
            } else {
                Toast.makeText(this, "Access Denied (${attemptCount}/${Constants.MAX_PIN_ATTEMPTS})", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN && AlarmHelper.isSirenActive) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    hiddenVolumeUpCounter++
                    hiddenVolumeUpHandler.removeCallbacks(hiddenVolumeUpReset)
                    hiddenVolumeUpHandler.postDelayed(hiddenVolumeUpReset, 3000L)
                    if (hiddenVolumeUpCounter >= 3) {
                        hiddenVolumeUpCounter = 0
                        hiddenVolumeUpHandler.removeCallbacks(hiddenVolumeUpReset)
                        AlarmHelper.stopSiren()
                        Toast.makeText(this, "Siren silenced. Enter PIN.", Toast.LENGTH_LONG).show()
                    }
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {}

    private fun fetchLocationAndUpdate() {
        val userId = FirebaseManager.getCurrentUser()?.uid ?: run { Log.w("AlarmOverlay", "No userId"); return }
        Log.d("AlarmOverlay", "fetchLocationAndUpdate starting for $userId")
        CoroutineScope(Dispatchers.IO).launch {
            repeat(5) { attempt ->
                try {
                    val location = LocationHelper.getCurrentLocation(this@AlarmOverlayActivity)
                    Log.d("AlarmOverlay", "attempt $attempt location: ${location?.latitude},${location?.longitude}")
                    if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                        val result = FirebaseManager.updateLocation(userId, location.latitude, location.longitude)
                        Log.d("AlarmOverlay", "updateLocation result: $result")
                        if (result.isSuccess) return@launch
                    }
                } catch (e: Exception) {
                    Log.e("AlarmOverlay", "attempt $attempt failed", e)
                }
                delay(5000)
            }
            Log.w("AlarmOverlay", "All 5 attempts exhausted — no valid location")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmHelper.stopSiren()
        focusHandler.removeCallbacks(reassertRunnable)
        if (receiverRegistered) {
            unregisterReceiver(alarmReceiver)
        }
    }
}
