package com.securphone.app.ui.maintenance

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.ActivityMaintenanceBinding
import com.securphone.app.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MaintenanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMaintenanceBinding
    private var pollJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaintenanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val message = PreferencesManager.getMaintenanceMessage(this)
        if (message.isNotBlank()) {
            binding.tvMessage.text = message
        }

        pollJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    FirebaseManager.fetchRemoteConfig()
                    FirebaseManager.getGlobalPolicyConfig(this@MaintenanceActivity)
                } catch (_: Exception) {}
                val spMode = PreferencesManager.isMaintenanceMode(this@MaintenanceActivity)
                val rcMode = FirebaseManager.isMaintenanceMode()
                if (!spMode && !rcMode) {
                    launch(Dispatchers.Main) {
                        val i = Intent(this@MaintenanceActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(i)
                        finish()
                    }
                    return@launch
                }
                delay(10000)
            }
        }
    }

    override fun onDestroy() {
        pollJob?.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
    }
}
