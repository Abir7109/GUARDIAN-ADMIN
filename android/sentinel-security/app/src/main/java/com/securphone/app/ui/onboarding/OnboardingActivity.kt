package com.securphone.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.ActivityOnboardingBinding
import com.securphone.app.ui.auth.LoginActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = OnboardingAdapter()
        binding.viewPager.adapter = adapter

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < adapter.itemCount - 1) {
                binding.viewPager.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        PreferencesManager.setOnboardingSeen(this, true)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
