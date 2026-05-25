package com.securphone.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.utils.Constants
import com.securphone.app.utils.TriggerManager
import com.securphone.app.utils.TriggerSource

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            val ctx = context ?: return
            if (!PreferencesManager.isProtectionActive(ctx)) return

            val bundle = intent.extras ?: return
            try {
                val pdus = bundle.get("pdus") as Array<*>? ?: return
                for (pdu in pdus) {
                    val format = bundle.getString("format") ?: "3gpp"
                    val byteArray = pdu as? ByteArray ?: continue
                    val message = SmsMessage.createFromPdu(byteArray, format)
                    val body = message.messageBody ?: ""
                    val sender = message.originatingAddress ?: continue

                    val triggerKeyword = PreferencesManager.getTriggerKeyword(ctx)
                    if (body.contains(triggerKeyword, ignoreCase = true)) {
                        TriggerManager.triggerBeast(
                            ctx,
                            source = TriggerSource.SMS,
                            senderNumber = sender
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
