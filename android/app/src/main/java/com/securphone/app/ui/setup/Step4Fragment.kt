package com.securphone.app.ui.setup

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.securphone.app.R
import com.securphone.app.databinding.FragmentStep4Binding
import com.securphone.app.services.SecurePhoneAccessibilityService

class Step4Fragment : Fragment() {

    private var _binding: FragmentStep4Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStep4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addHelpText()
        addSkipLink()
        updateUIState()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Step4", "accessibilityEnabled=${isAccessibilityServiceEnabled()}")
        updateUIState()
    }

    private fun addHelpText() {
        val help = TextView(requireContext()).apply {
            text = "Samsung users: Open Settings → Accessibility → Installed Apps → Guardian → toggle ON.\nOther devices: Settings → Accessibility → Guardian."
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            textSize = 11f
            gravity = android.view.Gravity.CENTER
            setPadding(16, 12, 16, 0)
        }
        (binding.root as? ViewGroup)?.addView(help)
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

    private fun updateUIState() {
        val enabled = isAccessibilityServiceEnabled()
        if (enabled) {
            binding.statusDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.status_green)
            binding.tvStatus.text = "Active"
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_green))
            binding.btnAction.text = "ENGAGED"
            binding.btnAction.setOnClickListener { (activity as? SetupWizardActivity)?.onStepCompleted() }
        } else {
            binding.statusDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.status_red)
            binding.tvStatus.text = "! Critical"
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_red))
            binding.btnAction.text = "ENABLE ENGINE"
            binding.btnAction.setOnClickListener { openAccessibilitySettings() }
        }
    }

    private fun openAccessibilitySettings() {
        val componentName = ComponentName(requireContext(), SecurePhoneAccessibilityService::class.java)

        // Attempt 1: Samsung "Installed Apps" accessibility screen (direct Samsung path)
        try {
            val intent = Intent("com.samsung.settings.ACCESSIBILITY_INSTALLED_APPS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return
        } catch (e: Exception) {
            Log.w("Step4", "Samsung installed apps intent failed", e)
        }

        // Attempt 2: AOSP accessibility details with ComponentName (Parcelable, not String)
        try {
            val intent = Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").apply {
                putExtra(Intent.EXTRA_COMPONENT_NAME, componentName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return
        } catch (e: Exception) {
            Log.w("Step4", "AOSP details intent failed", e)
        }

        // Attempt 3: Fallback to main accessibility settings
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e("Step4", "All accessibility intents failed", e)
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        try {
            val enabledServices = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
            val serviceName = ComponentName(requireContext(), SecurePhoneAccessibilityService::class.java).flattenToString()
            return enabledServices.contains(serviceName)
        } catch (e: Exception) {
            return false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
