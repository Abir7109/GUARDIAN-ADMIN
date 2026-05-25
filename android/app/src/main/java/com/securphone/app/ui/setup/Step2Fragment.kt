package com.securphone.app.ui.setup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.securphone.app.databinding.FragmentStep2Binding

class Step2Fragment : Fragment() {

    private var _binding: FragmentStep2Binding? = null
    private val binding get() = _binding!!

    private val requestFineLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        Log.d("Step2", "ACCESS_FINE_LOCATION granted=$granted")
        if (granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBgLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            updateUIState()
        }
    }

    private val requestBgLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        Log.d("Step2", "ACCESS_BACKGROUND_LOCATION granted=$granted")
        if (!granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openAppSettings()
        }
        updateUIState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStep2Binding.inflate(inflater, container, false)
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
        val fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val bgGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true

        val allGranted = fineGranted && bgGranted

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
                if (!fineGranted) {
                    requestFineLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else if (!bgGranted) {
                    requestBgLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
