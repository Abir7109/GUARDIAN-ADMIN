package com.securphone.app.ui.setup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.securphone.app.R
import com.securphone.app.databinding.FragmentStep1Binding
import com.securphone.app.utils.PermissionManager

class Step1Fragment : Fragment() {

    private var _binding: FragmentStep1Binding? = null
    private val binding get() = _binding!!

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        Log.d("Step1", "POST_NOTIFICATIONS granted=$granted")
        updateUIState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addSkipLink()
        updateUIState()
    }

    override fun onResume() {
        super.onResume()
        updateUIState()
    }

    private fun addSkipLink() {
        val skip = TextView(requireContext()).apply {
            text = "Skip this step"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            textSize = 13f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 24, 0, 0)
            setOnClickListener { (activity as? SetupWizardActivity)?.onStepSkipped() }
        }
        (binding.root as? ViewGroup)?.addView(skip)
    }

    private fun isNotificationListenerEnabled(): Boolean {
        return PermissionManager(requireContext()).isNotificationListenerEnabled()
    }

    private fun isPostNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun updateUIState() {
        val listenerOk = isNotificationListenerEnabled()
        val postOk = isPostNotificationsGranted()
        val allGranted = listenerOk && postOk

        if (allGranted) {
            binding.statusDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.status_green)
            binding.btnAction.text = "GRANTED"
            binding.btnAction.isEnabled = true
            binding.btnAction.setOnClickListener { (activity as? SetupWizardActivity)?.onStepCompleted() }
            binding.tvDescription.text = "Notification Listener access enabled — intercepting threat alerts in real-time."
        } else {
            binding.statusDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.status_amber)
            binding.btnAction.text = "AUTHORIZE"
            binding.btnAction.isEnabled = true
            binding.btnAction.setOnClickListener {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !postOk -> {
                        requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    !listenerOk -> {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                    else -> {
                        (activity as? SetupWizardActivity)?.onStepCompleted()
                    }
                }
            }
            if (!listenerOk && !postOk) {
                binding.tvDescription.text = "Two permissions needed:\n1. Allow notification posting (Android 13+)\n2. Enable Notification Listener access in system settings"
            } else if (!listenerOk) {
                binding.tvDescription.text = "Tap AUTHORIZE then toggle ON 'Guardian Notification Monitor' in the list."
            } else {
                binding.tvDescription.text = "Actively intercepting and analyzing incoming threat alerts in real-time."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
