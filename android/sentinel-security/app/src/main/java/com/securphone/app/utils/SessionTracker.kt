package com.securphone.app.utils

import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.models.SessionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

object SessionTracker {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var sessionStartTime: Long = 0
    private var currentSessionId: String = ""

    fun startSession() {
        sessionStartTime = System.currentTimeMillis()
        currentSessionId = UUID.randomUUID().toString()
    }

    fun endSession() {
        if (sessionStartTime == 0L) return
        val session = SessionModel(
            id = currentSessionId,
            userId = FirebaseManager.getCurrentUser()?.uid ?: "",
            startTime = sessionStartTime,
            endTime = System.currentTimeMillis(),
            duration = System.currentTimeMillis() - sessionStartTime,
            appVersion = Constants.CURRENT_VERSION
        )
        scope.launch {
            FirebaseManager.logSession(session)
        }
        sessionStartTime = 0
    }
}
