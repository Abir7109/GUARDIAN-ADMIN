package com.securphone.app.ui.update

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.ActivityForceUpdateBinding
import com.securphone.app.utils.Constants

class ForceUpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForceUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForceUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val msg = PreferencesManager.getUpdateMessage(this).takeIf { it.isNotBlank() }
            ?: FirebaseManager.getUpdateMessage().takeIf { it.isNotBlank() }
            ?: "A new version is available. Please update to continue using this app."
        binding.tvMessage.text = msg

        binding.btnUpdate.setOnClickListener {
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PLAY_STORE_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(playStoreIntent)
        }
    }

    override fun onBackPressed() {
    }
}
