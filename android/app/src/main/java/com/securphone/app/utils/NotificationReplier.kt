package com.securphone.app.utils

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

data class NotificationReplyAction(
    val pendingIntent: PendingIntent,
    val remoteInputs: Array<android.app.RemoteInput>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotificationReplyAction) return false
        return pendingIntent == other.pendingIntent
    }

    override fun hashCode(): Int = pendingIntent.hashCode()
}

object NotificationReplier {

    fun sendReply(context: Context, replyAction: NotificationReplyAction, replyText: String) {
        try {
            val remoteInput = replyAction.remoteInputs.firstOrNull() ?: return
            val intent = Intent()
            val results = Bundle()
            results.putCharSequence(remoteInput.resultKey, replyText)
            RemoteInput.addResultsToIntent(replyAction.remoteInputs, intent, results)
            replyAction.pendingIntent.send(context, 0, intent)
            Log.d("NotificationReplier", "Reply sent via RemoteInput: ${replyText.take(50)}...")
        } catch (e: Exception) {
            Log.e("NotificationReplier", "Failed to send RemoteInput reply", e)
        }
    }

    fun extractReplyAction(notification: Notification): NotificationReplyAction? {
        val actions = notification.actions ?: return null
        for (action in actions) {
            val inputs = action.remoteInputs
            if (!inputs.isNullOrEmpty()) {
                return NotificationReplyAction(
                    pendingIntent = action.actionIntent,
                    remoteInputs = inputs
                )
            }
        }
        return null
    }
}
