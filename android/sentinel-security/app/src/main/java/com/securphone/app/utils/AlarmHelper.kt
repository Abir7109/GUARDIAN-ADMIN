package com.securphone.app.utils

import android.content.Context
import com.securphone.app.services.SirenService

object AlarmHelper {
    @Volatile
    var isSirenActive = false

    @Volatile
    var sirenService: SirenService? = null

    fun startSiren(context: Context) {
        if (isSirenActive) return
        isSirenActive = true
        SirenService.start(context)
    }

    fun stopSiren() {
        if (!isSirenActive) return
        isSirenActive = false
        sirenService?.let { service ->
            try {
                service.stopSirenNow()
            } catch (_: Exception) {}
        }
    }
}
