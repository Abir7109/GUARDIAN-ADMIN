package com.securphone.app.utils

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.securphone.app.R

object OverlayHelper {
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var onPinVerifiedCallback: ((Boolean) -> Unit)? = null
    private var attemptCount = 0

    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun showPinOverlay(context: Context, onPinVerified: ((Boolean) -> Unit)? = null) {
        if (overlayView != null) return
        onPinVerifiedCallback = onPinVerified
        attemptCount = 0

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val inflater = LayoutInflater.from(context)
        overlayView = inflater.inflate(R.layout.dialog_pin_entry, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        val view = overlayView ?: return
        setupPinEntry(view, context)
        windowManager?.addView(overlayView, params)
    }

    fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
        overlayView = null
        windowManager = null
        onPinVerifiedCallback = null
    }

    private fun setupPinEntry(view: View, context: Context) {
        val pinInput = view.findViewById<EditText>(R.id.et_pin)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        btnConfirm?.setOnClickListener {
            val pin = pinInput?.text.toString()
            if (pin.length == 4) {
                val verified = PinManager.verifyPin(context, pin)
                if (verified) {
                    onPinVerifiedCallback?.invoke(true)
                    hideOverlay()
                } else {
                    attemptCount++
                    pinInput?.text?.clear()
                    pinInput?.startAnimation(
                        android.view.animation.AnimationUtils.loadAnimation(context, R.anim.shake)
                    )
                    if (attemptCount >= Constants.MAX_PIN_ATTEMPTS) {
                        onPinVerifiedCallback?.invoke(false)
                        hideOverlay()
                    }
                }
            }
        }

        btnCancel?.setOnClickListener {
            onPinVerifiedCallback?.invoke(false)
            hideOverlay()
        }
    }
}
