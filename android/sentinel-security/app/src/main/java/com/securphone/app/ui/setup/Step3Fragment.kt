package com.securphone.app.ui.setup

import android.content.Intent
import android.net.Uri
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
import com.securphone.app.databinding.FragmentStep3Binding

class Step3Fragment : Fragment() {

    private var _binding: FragmentStep3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addSkipLink()
        updateUIState()
    }

    override fun onResume() {
        super.onResume()
        val granted = Settings.canDrawOverlays(requireContext())
        Log.d("Step3", "canDrawOverlays=$granted")
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
        val granted = Settings.canDrawOverlays(requireContext())
        if (granted) {
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
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
