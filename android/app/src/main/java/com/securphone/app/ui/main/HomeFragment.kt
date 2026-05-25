package com.securphone.app.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.securphone.app.R
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.FragmentHomeBinding
import com.securphone.app.utils.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private var gpsDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivPowerToggle.setOnClickListener {
            viewModel.toggleSystemArm()
        }

        viewModel.isSystemArmed.observe(viewLifecycleOwner) { isArmed ->
            if (isArmed) {
                binding.tvStatusTitle.text = "System Protected"
                binding.tvStatusTitle.setTextColor(resources.getColor(R.color.white, null))
                binding.statusLed.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.tertiary, null))
                binding.tvStatusSubtext.text = "All protocols active"
                binding.ivPowerToggle.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.tertiary, null))
            } else {
                binding.tvStatusTitle.text = "Protection Disabled"
                binding.tvStatusTitle.setTextColor(resources.getColor(R.color.status_red, null))
                binding.statusLed.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.status_red, null))
                binding.tvStatusSubtext.text = "Warning: Sensors disarmed"
                binding.ivPowerToggle.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.status_red, null))
            }
        }

        viewModel.showGpsDialog.observe(viewLifecycleOwner) { show ->
            if (show) {
                showGpsDialog()
            }
        }

        setupSafeLocation()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupSafeLocation() {
        val ctx = requireContext()
        val enabled = PreferencesManager.isSafeLocationEnabled(ctx)
        binding.swSafeLocation.isChecked = enabled

        binding.wvSafeMap.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (enabled) {
                        val lat = PreferencesManager.getSafeZoneLat(ctx)
                        val lng = PreferencesManager.getSafeZoneLng(ctx)
                        val radius = PreferencesManager.getSafeZoneRadius(ctx)
                        view.evaluateJavascript("setLocation($lat, $lng, $radius);", null)
                    }
                }
            }
            loadUrl("file:///android_asset/safe_map.html")
        }

        updateSafeLocationUI(enabled)

        binding.btnSetSafeZone.setOnClickListener {
            if (!LocationHelper.isGpsEnabled(ctx)) {
                Toast.makeText(ctx, "Enable GPS to set safe zone", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                return@setOnClickListener
            }
            val intent = Intent(ctx, SafeZoneMapActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        binding.swSafeLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!LocationHelper.isGpsEnabled(ctx)) {
                    Toast.makeText(ctx, "Enable GPS to set safe zone", Toast.LENGTH_SHORT).show()
                    binding.swSafeLocation.isChecked = false
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    return@setOnCheckedChangeListener
                }
                Toast.makeText(ctx, "Getting current location...", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.IO).launch {
                    val location = LocationHelper.getCurrentLocation(ctx)
                    withContext(Dispatchers.Main) {
                        if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                            PreferencesManager.setSafeZoneLat(ctx, location.latitude)
                            PreferencesManager.setSafeZoneLng(ctx, location.longitude)
                            PreferencesManager.setSafeLocationEnabled(ctx, true)
                            val radius = PreferencesManager.getSafeZoneRadius(ctx)
                            binding.wvSafeMap.evaluateJavascript(
                                "setLocation(${location.latitude}, ${location.longitude}, $radius);", null
                            )
                            updateSafeLocationUI(true)
                            Toast.makeText(ctx, "Safe zone set!", Toast.LENGTH_SHORT).show()
                        } else {
                            binding.swSafeLocation.isChecked = false
                            Toast.makeText(ctx, "Could not get location. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                PreferencesManager.setSafeLocationEnabled(ctx, false)
                updateSafeLocationUI(false)
            }
        }
    }

    private fun updateSafeLocationUI(enabled: Boolean) {
        val ctx = requireContext()
        if (enabled) {
            val lat = PreferencesManager.getSafeZoneLat(ctx)
            val lng = PreferencesManager.getSafeZoneLng(ctx)
            val radius = PreferencesManager.getSafeZoneRadius(ctx)
            binding.tvSafeZoneStatus.text = "Safe zone active — ${radius.toInt()}m radius"
            binding.tvSafeCoordinates.text = String.format("%.4f, %.4f", lat, lng)
        } else {
            binding.tvSafeZoneStatus.text = "Set your safe zone"
            binding.tvSafeCoordinates.text = "Not set"
        }
    }

    private fun showGpsDialog() {
        gpsDialog?.dismiss()
        gpsDialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            .setTitle("Location Service Required")
            .setMessage("Location service must be turned on for protection features to track device location. Please enable GPS.")
            .setPositiveButton("Go to Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setOnDismissListener {
                viewModel.dismissGpsDialog()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        val ctx = requireContext()
        val enabled = PreferencesManager.isSafeLocationEnabled(ctx)
        binding.swSafeLocation.isChecked = enabled
        updateSafeLocationUI(enabled)
        if (enabled) {
            val lat = PreferencesManager.getSafeZoneLat(ctx)
            val lng = PreferencesManager.getSafeZoneLng(ctx)
            val radius = PreferencesManager.getSafeZoneRadius(ctx)
            binding.wvSafeMap.evaluateJavascript("setLocation($lat, $lng, $radius);", null)
        }
    }

    override fun onDestroyView() {
        gpsDialog?.dismiss()
        gpsDialog = null
        binding.wvSafeMap.destroy()
        super.onDestroyView()
        _binding = null
    }
}
