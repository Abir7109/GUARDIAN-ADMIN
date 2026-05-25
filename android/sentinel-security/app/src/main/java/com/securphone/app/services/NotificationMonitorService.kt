package com.securphone.app.services

import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.utils.Constants
import com.securphone.app.utils.NotificationReplier
import com.securphone.app.utils.TriggerManager
import com.securphone.app.utils.TriggerSource

class NotificationMonitorService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        if (!PreferencesManager.isProtectionActive(this)) {
            Log.d("NotifMonitor", "Protection inactive — ignoring notification from ${sbn.packageName}")
            return
        }

        val packageName = sbn.packageName
        val extras: Bundle? = sbn.notification.extras

        val title = extras?.getString("android.title") ?: ""
        val text = extractTextFromNotification(extras)

        Log.d("NotifMonitor", "Notification from package=$packageName title='$title' text='$text'")

        val isSmsPackage = packageName in Constants.SMS_PACKAGES
        val isWhatsAppPackage = packageName in Constants.WHATSAPP_PACKAGES
        if (!isSmsPackage && !isWhatsAppPackage) return

        Log.d("NotifMonitor", "Matches WhatsApp/SMS package — proceeding to keyword check")

        val source = if (isWhatsAppPackage) TriggerSource.WHATSAPP else TriggerSource.SMS
        val triggerKeyword = PreferencesManager.getTriggerKeyword(this)

        Log.d("NotifMonitor", "Checking keyword='$triggerKeyword' in title='$title' text='$text'")

        if (title.contains(triggerKeyword, ignoreCase = true) || text.contains(triggerKeyword, ignoreCase = true)) {
            Log.d("NotifMonitor", "KEYWORD MATCHED! Triggering beast mode...")

            val replyAction = if (isWhatsAppPackage) {
                NotificationReplier.extractReplyAction(sbn.notification)
            } else null

            if (replyAction != null) {
                Log.d("NotifMonitor", "Found notification reply action — will auto-reply via RemoteInput")
            }

            TriggerManager.triggerBeast(
                applicationContext,
                source = source,
                senderNumber = null,
                replyAction = replyAction
            )
        } else {
            Log.d("NotifMonitor", "No keyword match in this notification")
        }
    }

    private fun extractTextFromNotification(extras: Bundle?): String {
        if (extras == null) return ""

        // 1. Standard android.text (most common — WhatsApp single message)
        extras.getCharSequence("android.text")?.let {
            val s = it.toString().trim()
            if (s.isNotEmpty()) return s
        }

        // 2. MessagingStyle: android.messages array — extract last message text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            val messages = extras.getParcelableArray("android.messages")
            if (messages != null && messages.isNotEmpty()) {
                for (i in messages.indices.reversed()) {
                    val msg = messages[i]
                    if (msg is Bundle) {
                        msg.getString("text")?.let {
                            if (it.isNotEmpty()) return it
                        }
                    }
                }
            }
        }

        // 3. BigTextStyle: android.bigText
        extras.getCharSequence("android.bigText")?.let {
            val s = it.toString().trim()
            if (s.isNotEmpty()) return s
        }

        // 4. InboxStyle: android.textLines
        val textLines = extras.getCharSequenceArray("android.textLines")
        if (textLines != null && textLines.isNotEmpty()) {
            val combined = textLines.joinToString(" ") { it?.toString() ?: "" }.trim()
            if (combined.isNotEmpty()) return combined
        }

        // 5. Summary text fallback
        extras.getCharSequence("android.summaryText")?.let {
            val s = it.toString().trim()
            if (s.isNotEmpty()) return s
        }

        return ""
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
