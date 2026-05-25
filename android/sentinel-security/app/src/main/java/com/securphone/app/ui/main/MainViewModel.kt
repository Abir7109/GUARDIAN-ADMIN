package com.securphone.app.ui.main

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.services.ProtectionService
import com.securphone.app.ui.lock.GuardLockActivity
import com.securphone.app.utils.LocationHelper
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _isSystemArmed = MutableLiveData(PreferencesManager.isProtectionActive(application))
    val isSystemArmed: LiveData<Boolean> = _isSystemArmed

    private val _showGpsDialog = MutableLiveData(false)
    val showGpsDialog: LiveData<Boolean> = _showGpsDialog

    private val _isPocketShieldEnabled = MutableLiveData(PreferencesManager.isPocketShieldEnabled(application))
    val isPocketShieldEnabled: LiveData<Boolean> = _isPocketShieldEnabled

    private val _isChargerUnplugEnabled = MutableLiveData(PreferencesManager.isChargerUnplugEnabled(application))
    val isChargerUnplugEnabled: LiveData<Boolean> = _isChargerUnplugEnabled

    private val _isSimAlertEnabled = MutableLiveData(PreferencesManager.isSimAlertEnabled(application))
    val isSimAlertEnabled: LiveData<Boolean> = _isSimAlertEnabled

    private val _isUsbBlockEnabled = MutableLiveData(PreferencesManager.isUsbBlockEnabled(application))
    val isUsbBlockEnabled: LiveData<Boolean> = _isUsbBlockEnabled

    private val _isSafeLocationEnabled = MutableLiveData(PreferencesManager.isSafeLocationEnabled(application))
    val isSafeLocationEnabled: LiveData<Boolean> = _isSafeLocationEnabled

    fun toggleSystemArm() {
        val ctx = getApplication<Application>()
        val newState = !(_isSystemArmed.value ?: false)

        if (newState && !LocationHelper.isGpsEnabled(ctx)) {
            _showGpsDialog.value = true
            return
        }

        _isSystemArmed.value = newState
        PreferencesManager.setProtectionActive(ctx, newState)

        if (newState) {
            val intent = Intent(ctx, ProtectionService::class.java).apply {
                action = ProtectionService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
            val lockIntent = Intent(ctx, GuardLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            ctx.startActivity(lockIntent)
        } else {
            ctx.stopService(Intent(ctx, ProtectionService::class.java))
        }

        viewModelScope.launch {
            FirebaseManager.getCurrentUser()?.uid?.let { uid ->
                FirebaseManager.toggleShield(uid, newState)
            }
        }
    }

    fun dismissGpsDialog() {
        _showGpsDialog.value = false
    }

    fun updatePocketShield(enabled: Boolean) {
        _isPocketShieldEnabled.value = enabled
        PreferencesManager.setPocketShieldEnabled(getApplication(), enabled)
    }

    fun updateChargerUnplug(enabled: Boolean) {
        _isChargerUnplugEnabled.value = enabled
        PreferencesManager.setChargerUnplugEnabled(getApplication(), enabled)
    }

    fun updateSimAlert(enabled: Boolean) {
        _isSimAlertEnabled.value = enabled
        PreferencesManager.setSimAlertEnabled(getApplication(), enabled)
    }

    fun updateUsbBlock(enabled: Boolean) {
        _isUsbBlockEnabled.value = enabled
        PreferencesManager.setUsbBlockEnabled(getApplication(), enabled)
    }

    fun updateSafeLocation(enabled: Boolean) {
        _isSafeLocationEnabled.value = enabled
        PreferencesManager.setSafeLocationEnabled(getApplication(), enabled)
    }
}
