package com.securphone.app.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.securphone.app.databinding.ActivitySplashBinding
import com.securphone.app.ui.auth.LoginActivity
import com.securphone.app.ui.main.MainActivity
import com.securphone.app.ui.maintenance.MaintenanceActivity
import com.securphone.app.ui.onboarding.OnboardingActivity
import com.securphone.app.ui.update.ForceUpdateActivity
import com.securphone.app.ui.setup.SetupWizardActivity
import com.securphone.app.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(Constants.MIN_SPLASH_DURATION_MS)
            viewModel.determineRoute()
        }

        viewModel.route.observe(this) { routeState ->
            val nextIntent: Intent? = when (routeState) {
                SplashViewModel.RouteState.ONBOARDING -> Intent(this, OnboardingActivity::class.java)
                SplashViewModel.RouteState.LOGIN -> Intent(this, LoginActivity::class.java)
                SplashViewModel.RouteState.SETUP_WIZARD -> Intent(this, SetupWizardActivity::class.java)
                SplashViewModel.RouteState.MAINTENANCE -> Intent(this, MaintenanceActivity::class.java)
                SplashViewModel.RouteState.FORCE_UPDATE -> Intent(this, ForceUpdateActivity::class.java)
                SplashViewModel.RouteState.MAIN -> Intent(this, MainActivity::class.java)
                else -> null
            }
            nextIntent?.let {
                startActivity(it)
                finish()
            }
        }
    }
}
