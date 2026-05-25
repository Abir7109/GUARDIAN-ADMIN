package com.securphone.app

import android.app.Application
import com.securphone.app.data.firebase.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SecurePhoneApp : Application() {

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this

        FirebaseManager.initialize(this)

        appScope.launch {
            FirebaseManager.fetchRemoteConfig()
        }
    }

    companion object {
        lateinit var instance: SecurePhoneApp
            private set
    }
}
