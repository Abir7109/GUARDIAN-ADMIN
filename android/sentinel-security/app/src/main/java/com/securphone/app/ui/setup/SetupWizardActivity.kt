package com.securphone.app.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.securphone.app.R
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.ActivitySetupWizardBinding
import com.securphone.app.ui.main.MainActivity

class SetupWizardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupWizardBinding
    private val viewModel: SetupWizardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupWizardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.currentStep.observe(this) { step ->
            updateStepFragment(step)
            updateHeaders(step)
        }

        viewModel.securedCount.observe(this) { count ->
            binding.tvSecuredCount.text = "$count / 6 Secured"
            binding.pbSetup.progress = count * 100 / 6
        }
    }

    fun onStepCompleted() {
        viewModel.nextStep()
    }

    fun onStepSkipped() {
        viewModel.skipStep()
    }

    fun onSetupComplete() {
        PreferencesManager.setSetupCompleted(this, true)
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    private fun updateStepFragment(step: Int) {
        val fragment: Fragment = when (step) {
            1 -> Step1Fragment()
            2 -> Step2Fragment()
            3 -> Step3Fragment()
            4 -> Step4Fragment()
            5 -> Step5Fragment()
            6 -> Step6Fragment()
            else -> Step1Fragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateHeaders(step: Int) {
        binding.tvPhase.text = "Step $step of 6"
        binding.tvTitle.text = when (step) {
            1 -> "Notification Access"
            2 -> "Background Location"
            3 -> "Screen Overlay"
            4 -> "Accessibility Engine"
            5 -> "Comms Override"
            6 -> "Set Your PIN"
            else -> "System Protocols"
        }
    }
}
