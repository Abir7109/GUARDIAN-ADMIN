package com.securphone.app.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securphone.app.R
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.ActivitySafeZoneMapBinding

class SafeZoneMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySafeZoneMapBinding
    private var pendingLat: Double = 0.0
    private var pendingLng: Double = 0.0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySafeZoneMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        supportActionBar?.hide()

        val currentLat = PreferencesManager.getSafeZoneLat(this)
        val currentLng = PreferencesManager.getSafeZoneLng(this)
        val currentRadius = PreferencesManager.getSafeZoneRadius(this)

        pendingLat = currentLat
        pendingLng = currentLng

        binding.wvMap.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            addJavascriptInterface(MapBridge(), "AndroidBridge")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (pendingLat != 0.0 || pendingLng != 0.0) {
                        view.evaluateJavascript("setLocation($pendingLat, $pendingLng, $currentRadius);", null)
                    }
                }
            }
            loadUrl("file:///android_asset/safe_map.html")
        }

        binding.btnConfirm.setOnClickListener {
            binding.wvMap.evaluateJavascript("getLocation();") { result ->
                try {
                    val json = result?.removeSurrounding("\"")?.replace("\\\"", "\"") ?: return@evaluateJavascript
                    val obj = org.json.JSONObject(json)
                    val lat = obj.getDouble("lat")
                    val lng = obj.getDouble("lng")
                    val radius = obj.getDouble("radius")
                    PreferencesManager.setSafeZoneLat(this@SafeZoneMapActivity, lat)
                    PreferencesManager.setSafeZoneLng(this@SafeZoneMapActivity, lng)
                    PreferencesManager.setSafeZoneRadius(this@SafeZoneMapActivity, radius)
                    PreferencesManager.setSafeLocationEnabled(this@SafeZoneMapActivity, true)
                    Toast.makeText(this, "Safe zone saved!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error saving location", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnClose.setOnClickListener { finish() }
    }

    private inner class MapBridge {
        @JavascriptInterface
        fun onLocationChanged(lat: Double, lng: Double) {
            pendingLat = lat
            pendingLng = lng
        }
    }
}
