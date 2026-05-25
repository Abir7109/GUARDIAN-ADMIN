package com.securphone.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.securphone.app.databinding.FragmentEmergencyBinding
import com.securphone.app.utils.AlarmHelper

class EmergencyFragment : Fragment() {

    private var _binding: FragmentEmergencyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEmergencySiren.text = if (AlarmHelper.isSirenActive) "STOP SIREN ALARM" else "ACTIVATE SIREN PANIC"

        binding.btnEmergencySiren.setOnClickListener {
            context?.let { ctx ->
                if (AlarmHelper.isSirenActive) {
                    AlarmHelper.stopSiren()
                    binding.btnEmergencySiren.text = "ACTIVATE SIREN PANIC"
                } else {
                    AlarmHelper.startSiren(ctx)
                    binding.btnEmergencySiren.text = "STOP SIREN ALARM"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
