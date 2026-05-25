package com.securphone.app.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.FragmentStep6Binding
import com.securphone.app.utils.Constants
import com.securphone.app.utils.PinManager
import kotlinx.coroutines.launch

class Step6Fragment : Fragment() {

    private var _binding: FragmentStep6Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStep6Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAction.setOnClickListener { handleSavePin() }
    }

    private fun handleSavePin() {
        val pin = binding.etPin.text?.toString()?.trim() ?: ""
        val confirmPin = binding.etConfirmPin.text?.toString()?.trim() ?: ""

        binding.tvPinError.visibility = View.GONE

        if (pin.length < 4) {
            binding.tilPin.error = "Enter a 4-digit PIN"
            return
        }
        if (confirmPin.length < 4) {
            binding.tilConfirmPin.error = "Confirm your PIN"
            return
        }
        if (pin != confirmPin) {
            binding.tvPinError.visibility = View.VISIBLE
            binding.etPin.text?.clear()
            binding.etConfirmPin.text?.clear()
            return
        }

        binding.tilPin.error = null
        binding.tilConfirmPin.error = null
        PinManager.savePin(requireContext(), pin)

        lifecycleScope.launch {
            FirebaseManager.getCurrentUser()?.uid?.let { uid ->
                FirebaseManager.updateUserDocument(uid, mapOf(
                    Constants.FIELD_PIN_HASH to (PreferencesManager.getPinHash(requireContext()) ?: ""),
                    Constants.FIELD_PIN_SALT to (PreferencesManager.getPinSalt(requireContext()) ?: "")
                ))
            }
        }

        (activity as? SetupWizardActivity)?.onSetupComplete()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
