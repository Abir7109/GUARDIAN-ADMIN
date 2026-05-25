package com.securphone.app.receivers

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.models.EventModel
import com.securphone.app.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SecurePhoneDeviceAdmin : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Device admin granted", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Device admin disabled", Toast.LENGTH_LONG).show()
        CoroutineScope(Dispatchers.IO).launch {
            val userId = FirebaseManager.getCurrentUser()?.uid ?: return@launch
            FirebaseManager.logEvent(EventModel(
                userId = userId,
                type = Constants.EVENT_ADMIN_DISABLED,
                timestamp = System.currentTimeMillis()
            ))
        }
    }
}
