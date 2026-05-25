package com.securphone.app.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.securphone.app.R
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.data.models.EventModel
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.FragmentProfileBinding
import com.securphone.app.ui.splash.SplashActivity
import com.securphone.app.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var currentUserName = ""
    private var currentUserPhone = ""
    private var currentUserEmail = ""
    private var emergencyContacts: MutableList<Pair<String, String>> = mutableListOf()

    private val contactPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { contactUri ->
                readContact(contactUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.btnLogout.setOnClickListener {
            FirebaseManager.signOut()
            PreferencesManager.clearAll(requireContext())
            val intent = Intent(activity, SplashActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            activity?.finish()
        }

        binding.llEditProfile.setOnClickListener { showEditProfileDialog() }
        binding.llAlertPrefs.setOnClickListener { showAlertPrefsDialog() }
        binding.btnAddContact.setOnClickListener { showAddContactDialog() }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        loadEmergencyContacts()
        renderContacts()
        loadSecurityLogs()
    }

    private fun loadUserData() {
        val uid = FirebaseManager.getCurrentUser()?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val result = FirebaseManager.getUserDocument(uid)
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let { u ->
                        currentUserName = u.displayName.ifBlank { "User" }
                        currentUserPhone = u.phone
                        currentUserEmail = u.email
                        binding.tvUserName.text = currentUserName
                        val level = u.clearanceLevel
                        binding.tvClearanceBadge.text = "LEVEL $level CLEARANCE"
                        binding.tvAccountStatus.text = when (u.memberStatus) {
                            Constants.STATUS_PREMIUM -> "Premium Active"
                            else -> "Free"
                        }
                        binding.tvAccountStatus.setTextColor(
                            resources.getColor(
                                if (u.memberStatus == Constants.STATUS_PREMIUM) R.color.primary_light else R.color.text_secondary,
                                null
                            )
                        )
                        binding.tvUserEmail.text = u.email
                        binding.tvUserEmail.visibility = View.VISIBLE
                        binding.tvUserPhone.text = u.phone.ifBlank { "No phone set" }
                        binding.tvUserPhone.visibility = View.VISIBLE
                    }
                }
            }
        }
        loadEmergencyContacts()
        renderContacts()
        loadSecurityLogs()
    }

    private fun showEditProfileDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
        builder.setTitle("Edit Profile")

        val nameInput = TextInputEditText(requireContext()).apply {
            setText(currentUserName)
            hint = "Display Name"
            setTextColor(resources.getColor(R.color.white, null))
            setHintTextColor(resources.getColor(R.color.text_secondary, null))
        }

        val phoneInput = TextInputEditText(requireContext()).apply {
            setText(currentUserPhone)
            hint = "Phone Number"
            setTextColor(resources.getColor(R.color.white, null))
            setHintTextColor(resources.getColor(R.color.text_secondary, null))
        }

        val ll = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 40)
            addView(nameInput, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) })
            addView(phoneInput)
        }

        builder.setView(ll)
        builder.setPositiveButton("Save") { _, _ ->
            val newName = nameInput.text?.toString()?.trim() ?: ""
            val newPhone = phoneInput.text?.toString()?.trim() ?: ""
            if (newName.isNotEmpty()) {
                saveProfile(newName, newPhone)
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun saveProfile(displayName: String, phone: String) {
        val uid = FirebaseManager.getCurrentUser()?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val updates = mapOf<String, Any>(
                Constants.FIELD_DISPLAY_NAME to displayName,
                Constants.FIELD_PHONE to phone
            )
            val result = FirebaseManager.updateUserDocument(uid, updates)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    loadUserData()
                } else {
                    Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAlertPrefsDialog() {
        val ctx = requireContext()
        val currentKeyword = PreferencesManager.getTriggerKeyword(ctx)

        val builder = AlertDialog.Builder(ctx, R.style.AlertDialogCustom)
        builder.setTitle("Alert Preferences")

        val keywordInput = TextInputEditText(ctx).apply {
            setText(currentKeyword)
            hint = "Trigger Keyword"
            setTextColor(resources.getColor(R.color.white, null))
            setHintTextColor(resources.getColor(R.color.text_secondary, null))
        }

        val infoTv = TextView(ctx).apply {
            text = "When someone texts this keyword, the alarm will trigger"
            setTextColor(resources.getColor(R.color.text_secondary, null))
            textSize = 12f
            setPadding(0, 0, 0, 20)
        }

        val ll = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 24)
            addView(infoTv)
            addView(keywordInput)
        }

        builder.setView(ll)
        builder.setPositiveButton("Save") { _, _ ->
            val newKeyword = keywordInput.text?.toString()?.trim() ?: ""
            if (newKeyword.isNotEmpty()) {
                PreferencesManager.setTriggerKeyword(ctx, newKeyword)
                Toast.makeText(ctx, "Keyword updated to \"$newKeyword\"", Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun loadEmergencyContacts() {
        emergencyContacts.clear()
        val contacts = PreferencesManager.getEmergencyContacts(requireContext())
        for (entry in contacts) {
            val parts = entry.split("|", limit = 2)
            if (parts.size == 2) {
                emergencyContacts.add(Pair(parts[0], parts[1]))
            }
        }
    }

    private fun saveEmergencyContacts() {
        val set = emergencyContacts.map { "${it.first}|${it.second}" }.toSet()
        PreferencesManager.setEmergencyContacts(requireContext(), set)
    }

    private fun renderContacts() {
        binding.llContactsContainer.removeAllViews()
        if (emergencyContacts.isEmpty()) {
            binding.tvNoContacts.visibility = View.VISIBLE
            return
        }
        binding.tvNoContacts.visibility = View.GONE
        for ((index, contact) in emergencyContacts.withIndex()) {
            val row = createContactRow(index, contact)
            binding.llContactsContainer.addView(row)
        }
    }

    private fun createContactRow(index: Int, contact: Pair<String, String>): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.bg_square)
            backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.surface_container_high, null)
            setPadding(14, 12, 14, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 8) }
        }

        val infoLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        infoLayout.addView(TextView(requireContext()).apply {
            text = contact.first
            setTextColor(resources.getColor(R.color.white, null))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })

        infoLayout.addView(TextView(requireContext()).apply {
            text = contact.second
            setTextColor(resources.getColor(R.color.text_secondary, null))
            textSize = 12f
        })

        row.addView(infoLayout)

        val callBtn = Button(requireContext()).apply {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone_stub, 0, 0, 0)
            backgroundTintList = ResourcesCompat.getColorStateList(resources, android.R.color.transparent, null)
            layoutParams = LinearLayout.LayoutParams(80, 80)
            setOnClickListener { dialNumber("tel:${contact.second}") }
        }

        val deleteBtn = TextView(requireContext()).apply {
            text = "✕"
            textSize = 18f
            setTextColor(resources.getColor(R.color.status_red, null))
            setPadding(12, 0, 0, 0)
            setOnClickListener {
                emergencyContacts.removeAt(index)
                saveEmergencyContacts()
                renderContacts()
                Toast.makeText(context, "Contact removed", Toast.LENGTH_SHORT).show()
            }
        }

        row.addView(callBtn)
        row.addView(deleteBtn)
        return row
    }

    private fun showAddContactDialog() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        contactPickerLauncher.launch(intent)
    }

    private fun readContact(contactUri: Uri) {
        val ctx = requireContext()
        var name = ""
        var phone = ""

        val cursor = ctx.contentResolver.query(contactUri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: ""
                phone = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                phone = phone.replace(Regex("[^0-9+]"), "")
            }
        }

        if (name.isNotEmpty() && phone.isNotEmpty()) {
            emergencyContacts.add(Pair(name, phone))
            saveEmergencyContacts()
            renderContacts()
            Toast.makeText(ctx, "$name added", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(ctx, "Contact has no phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSecurityLogs() {
        val uid = FirebaseManager.getCurrentUser()?.uid ?: return
        binding.llLogs.removeAllViews()
        addLogText("Loading logs...", R.color.text_secondary)

        CoroutineScope(Dispatchers.IO).launch {
            val result = FirebaseManager.getEvents(userId = uid, limit = 10)
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                binding.llLogs.removeAllViews()
                if (result.isSuccess) {
                    val events = result.getOrDefault(emptyList())
                    if (events.isEmpty()) {
                        addLogText("No security events yet", R.color.text_secondary)
                    } else {
                        for (event in events) {
                            binding.llLogs.addView(createEventRow(event))
                        }
                    }
                } else {
                    addLogText("Failed to load logs", R.color.status_red)
                }
            }
        }
    }

    private fun addLogText(text: String, colorId: Int) {
        binding.llLogs.addView(TextView(requireContext()).apply {
            this.text = text
            setTextColor(resources.getColor(colorId, null))
            textSize = 13f
        })
    }

    private fun createEventRow(event: EventModel): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_square)
            backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.surface_container_high, null)
            setPadding(12, 12, 12, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 8) }
        }

        row.addView(TextView(requireContext()).apply {
            text = formatEventTitle(event)
            setTextColor(resources.getColor(R.color.white, null))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })

        row.addView(TextView(requireContext()).apply {
            text = formatEventDetail(event)
            setTextColor(resources.getColor(R.color.text_secondary, null))
            textSize = 11f
            setPadding(0, 4, 0, 0)
        })

        return row
    }

    private fun formatEventTitle(event: EventModel): String {
        return when (event.type) {
            Constants.EVENT_TRIGGER_ACTIVATED -> "Trigger Activated"
            Constants.EVENT_ALARM_STARTED -> "Alarm Started"
            Constants.EVENT_EMERGENCY_TRIGGERED -> "Emergency Triggered"
            Constants.EVENT_LOCATION_SENT -> "Location Sent"
            Constants.EVENT_PIN_CORRECT -> "PIN Correct"
            Constants.EVENT_PIN_FAILED -> "PIN Failed"
            Constants.EVENT_SHIELD_ACTIVATED -> "Protection Activated"
            Constants.EVENT_SHIELD_DEACTIVATED -> "Protection Deactivated"
            else -> event.type.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatEventDetail(event: EventModel): String {
        val time = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(event.timestamp))
        val details = mutableListOf<String>()
        if (event.triggerSource.isNotEmpty()) details.add("Source: ${event.triggerSource}")
        if (event.triggerNumber.isNotEmpty()) details.add("From: ${event.triggerNumber}")
        if (event.latitude != 0.0 || event.longitude != 0.0) {
            details.add("Loc: ${String.format("%.4f,%.4f", event.latitude, event.longitude)}")
        }
        details.add(time)
        return details.joinToString(" · ")
    }

    private fun dialNumber(uriString: String) {
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(uriString)))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to dial", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
