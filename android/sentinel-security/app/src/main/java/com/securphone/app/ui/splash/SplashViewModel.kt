package com.securphone.app.ui.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _route = MutableLiveData<RouteState>()
    val route: LiveData<RouteState> = _route

    fun determineRoute() {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseManager.fetchRemoteConfig()
        }
        val ctx = getApplication<Application>()

        if (isForceUpdateRequired(ctx)) {
            _route.value = RouteState.FORCE_UPDATE
        } else if (isMaintenanceMode(ctx)) {
            _route.value = RouteState.MAINTENANCE
        } else if (!PreferencesManager.isOnboardingSeen(ctx)) {
            _route.value = RouteState.ONBOARDING
        } else if (FirebaseManager.getCurrentUser() == null) {
            _route.value = RouteState.LOGIN
        } else if (!PreferencesManager.isSetupCompleted(ctx)) {
            _route.value = RouteState.SETUP_WIZARD
        } else {
            _route.value = RouteState.MAIN
        }
    }

    private fun isMaintenanceMode(ctx: Application): Boolean {
        return try {
            PreferencesManager.isMaintenanceMode(ctx) || FirebaseManager.isMaintenanceMode()
        } catch (_: Exception) {
            false
        }
    }

    private fun isForceUpdateRequired(ctx: Application): Boolean {
        return try {
            val enabled = PreferencesManager.isForceUpdateEnabled(ctx)
            val minVersion = if (enabled) {
                PreferencesManager.getMinRequiredVersion(ctx)
            } else {
                if (!FirebaseManager.isForceUpdateRequired()) return false
                FirebaseManager.getMinimumVersion()
            }
            if (minVersion.isBlank()) return false
            isVersionLowerThan(Constants.CURRENT_VERSION, minVersion)
        } catch (_: Exception) {
            false
        }
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

    enum class RouteState {
        ONBOARDING, LOGIN, SETUP_WIZARD, MAIN, MAINTENANCE, FORCE_UPDATE
    }
}
