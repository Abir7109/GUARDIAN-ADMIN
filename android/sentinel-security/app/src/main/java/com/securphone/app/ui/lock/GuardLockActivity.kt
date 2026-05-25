package com.securphone.app.ui.lock

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securphone.app.R
import com.securphone.app.databinding.ActivityGuardLockBinding
import com.securphone.app.utils.Constants
import com.securphone.app.utils.PinManager
import com.securphone.app.utils.shake

class GuardLockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardLockBinding
    private val enteredPin = StringBuilder()
    private var attemptCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PinManager.isPinSet(this)) {
            Toast.makeText(this, "No PIN configured — unlocking", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KeyguardManager::class.java)
            km?.requestDismissKeyguard(this, null)
        }

        binding = ActivityGuardLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.let {
            it.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }

        setupKeypad()
        binding.btnUnlock.setOnClickListener { verifyAndUnlock() }
    }

    private fun setupKeypad() {
        binding.btnKey1.setOnClickListener { appendDigit("1") }
        binding.btnKey2.setOnClickListener { appendDigit("2") }
        binding.btnKey3.setOnClickListener { appendDigit("3") }
        binding.btnKey4.setOnClickListener { appendDigit("4") }
        binding.btnKey5.setOnClickListener { appendDigit("5") }
        binding.btnKey6.setOnClickListener { appendDigit("6") }
        binding.btnKey7.setOnClickListener { appendDigit("7") }
        binding.btnKey8.setOnClickListener { appendDigit("8") }
        binding.btnKey9.setOnClickListener { appendDigit("9") }
        binding.btnKey0.setOnClickListener { appendDigit("0") }
        binding.btnKeyDel.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin.deleteCharAt(enteredPin.length - 1)
                updateDots()
            }
        }
    }

    private fun appendDigit(digit: String) {
        if (enteredPin.length < 4) {
            enteredPin.append(digit)
            updateDots()
            if (enteredPin.length == 4) {
                verifyAndUnlock()
            }
        }
    }

    private fun updateDots() {
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
            Toast.makeText(this, "Device Unlocked", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            binding.cvLockHeader.shake()
            enteredPin.clear()
            updateDots()
            if (attemptCount >= Constants.MAX_PIN_ATTEMPTS) {
                Toast.makeText(this, "Too many failed attempts — device locked", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {}
}
