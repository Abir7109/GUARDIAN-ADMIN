package com.securphone.app.utils

import android.content.Context
import com.securphone.app.data.preferences.PreferencesManager

object PinManager {
    fun savePin(context: Context, pin: String) {
        val salt = CryptoUtils.generateSalt()
        val hash = CryptoUtils.hashPin(pin, salt)
        PreferencesManager.setPinHash(context, hash)
        PreferencesManager.setPinSalt(context, salt)
    }

    fun verifyPin(context: Context, enteredPin: String): Boolean {
        val storedHash = PreferencesManager.getPinHash(context) ?: return false
        val storedSalt = PreferencesManager.getPinSalt(context) ?: return false
        return CryptoUtils.verifyPin(enteredPin, storedSalt, storedHash)
    }

    fun isPinSet(context: Context): Boolean {
        return PreferencesManager.getPinHash(context) != null
    }

    fun getPinHash(context: Context): String {
        return PreferencesManager.getPinHash(context) ?: ""
    }

    fun getPinSalt(context: Context): String {
        return PreferencesManager.getPinSalt(context) ?: ""
    }
}
