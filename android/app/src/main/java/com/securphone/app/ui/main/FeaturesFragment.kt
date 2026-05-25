package com.securphone.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.securphone.app.databinding.FragmentFeaturesBinding

class FeaturesFragment : Fragment() {

    private var _binding: FragmentFeaturesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeaturesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isPocketShieldEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.swPocketShield.isChecked = enabled
            binding.tvShieldStatus.text = if (enabled) "Active" else "Inactive"
        }
        viewModel.isChargerUnplugEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.swCharger.isChecked = enabled
            binding.tvChargerStatus.text = if (enabled) "Active" else "Inactive"
        }
        viewModel.isSimAlertEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.swSim.isChecked = enabled
            binding.tvSimStatus.text = if (enabled) "Active" else "Inactive"
        }
        viewModel.isUsbBlockEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.swUsb.isChecked = enabled
            binding.tvUsbStatus.text = if (enabled) "Active" else "Inactive"
        }

        binding.swPocketShield.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updatePocketShield(isChecked)
        }
        binding.swCharger.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateChargerUnplug(isChecked)
        }
        binding.swSim.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSimAlert(isChecked)
        }
        binding.swUsb.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateUsbBlock(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
