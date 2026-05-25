package com.securphone.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

object LocationHelper {
    private const val TAG = "SecurphoneLoc"

    fun initialize(context: Context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!fineGranted) {
            Log.w(TAG, "FINE_LOCATION not granted")
            return getLastKnownLocation(context)
        }

        val lastKnown = getLastKnownLocation(context)
        Log.d(TAG, "lastKnownLocation: ${lastKnown?.latitude},${lastKnown?.longitude}")

        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(60000)
                .build()
            val loc = client.getCurrentLocation(request, CancellationTokenSource().token).await()
            Log.d(TAG, "fused getCurrentLocation: ${loc?.latitude},${loc?.longitude}")
            if (loc != null && (loc.latitude != 0.0 || loc.longitude != 0.0)) return loc
        } catch (e: Exception) {
            Log.e(TAG, "fused getCurrentLocation failed", e)
        }

        if (lastKnown != null && (lastKnown.latitude != 0.0 || lastKnown.longitude != 0.0)) return lastKnown

        return requestFreshLocation(context)
    }

    private suspend fun requestFreshLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) { Log.w(TAG, "No LocationManager"); return null }

        return suspendCancellableCoroutine { cont ->
            val timeoutMs = 30000L
            var completed = false
            val timeoutRunnable = Runnable {
                if (!completed) {
                    completed = true
                    val fallback = getLastKnownLocation(context)
                    Log.d(TAG, "requestFreshLocation timeout, fallback: ${fallback?.latitude},${fallback?.longitude}")
                    cont.resume(fallback)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val executor = context.mainExecutor
                val gpsConsumer = java.util.function.Consumer<Location?> { location ->
                    if (!completed && location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                        completed = true
                        Handler(Looper.getMainLooper()).removeCallbacks(timeoutRunnable)
                        Log.d(TAG, "GPS getCurrentLocation (API31+): ${location.latitude},${location.longitude}")
                        cont.resume(location)
                    }
                }
                val netConsumer = java.util.function.Consumer<Location?> { location ->
                    if (!completed && location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                        completed = true
                        Handler(Looper.getMainLooper()).removeCallbacks(timeoutRunnable)
                        Log.d(TAG, "NET getCurrentLocation (API31+): ${location.latitude},${location.longitude}")
                        cont.resume(location)
                    }
                }
                try { lm.getCurrentLocation(LocationManager.GPS_PROVIDER, null, executor, gpsConsumer) } catch (_: Exception) {}
                try { lm.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, executor, netConsumer) } catch (_: Exception) {}
            } else {
                // Pre-Android12 — use deprecated requestSingleUpdate
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (!completed && (location.latitude != 0.0 || location.longitude != 0.0)) {
                            completed = true
                            Handler(Looper.getMainLooper()).removeCallbacks(timeoutRunnable)
                            Log.d(TAG, "requestSingleUpdate: ${location.latitude},${location.longitude}")
                            cont.resume(location)
                        }
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                val enabledProviders = lm.getProviders(true)
                var requested = false
                for (provider in enabledProviders) {
                    try {
                        lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                        requested = true
                    } catch (_: Exception) {}
                }
                if (!requested) {
                    try { lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper()) } catch (_: Exception) {}
                    try { lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, Looper.getMainLooper()) } catch (_: Exception) {}
                }
            }

            Handler(Looper.getMainLooper()).postDelayed(timeoutRunnable, timeoutMs)

            cont.invokeOnCancellation {
                if (!completed) { completed = true; Handler(Looper.getMainLooper()).removeCallbacks(timeoutRunnable) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return try {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (_: Exception) { null }
    }

    fun isGpsEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}
