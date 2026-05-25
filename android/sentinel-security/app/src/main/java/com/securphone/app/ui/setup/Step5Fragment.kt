package com.securphone.app.ui.setup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.securphone.app.R
import com.securphone.app.databinding.FragmentStep5Binding

class Step5Fragment : Fragment() {

    private var _binding: FragmentStep5Binding? = null
    private val binding get() = _binding!!

    private val requestSms = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        Log.d("Step5", "SEND_SMS granted=$granted")
        if (granted) {
            requestPhoneState.launch(Manifest.permission.READ_PHONE_STATE)
        } else {
            updateUIState()
        }
    }

    private val requestPhoneState = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        Log.d("Step5", "READ_PHONE_STATE granted=$granted")
        updateUIState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStep5Binding.inflate(inflater, container, false)
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

    private fun updateUIState() {
        val smsGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val phoneGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

        val allGranted = smsGranted && phoneGranted

        if (allGranted) {
            binding.statusDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.status_green)
            binding.tvStatus.text = "Granted"
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_green))
            binding.btnAction.text = "GRANTED"
            binding.btnAction.setOnClickListener { (activity as? SetupWizardActivity)?.onStepCompleted() }
        } else {
            binding.statusDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.status_amber)
            binding.tvStatus.text = "Pending"
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_amber))
            binding.btnAction.text = "AUTHORIZE"
            binding.btnAction.setOnClickListener {
                if (!smsGranted) {
                    requestSms.launch(Manifest.permission.SEND_SMS)
                } else if (!phoneGranted) {
                    requestPhoneState.launch(Manifest.permission.READ_PHONE_STATE)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
