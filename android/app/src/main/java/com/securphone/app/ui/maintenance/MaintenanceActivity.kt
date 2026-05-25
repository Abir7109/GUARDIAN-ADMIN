package com.securphone.app.ui.maintenance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.ActivityMaintenanceBinding

class MaintenanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMaintenanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaintenanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val message = PreferencesManager.getMaintenanceMessage(this)
        if (message.isNotBlank()) {
            binding.tvMessage.text = message
        }
    }

    override fun onBackPressed() {
    }
}
