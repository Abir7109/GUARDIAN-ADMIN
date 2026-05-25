package com.securphone.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.ActivitySignupBinding
import com.securphone.app.ui.main.MainActivity
import com.securphone.app.ui.setup.SetupWizardActivity
import com.securphone.app.utils.shake

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTabLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val name = binding.etFullname.text.toString().trim()
            val code = binding.etAccessCode.text.toString().trim()
            viewModel.signUp(email, code, name)
        }

        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.btnSignup.isEnabled = false
                    binding.btnSignup.text = "REGISTERING PROTOCOL..."
                }
                is AuthViewModel.AuthState.Success -> {
                    val target = if (PreferencesManager.isSetupCompleted(this)) {
                        Intent(this, MainActivity::class.java)
                    } else {
                        Intent(this, SetupWizardActivity::class.java)
                    }
                    startActivity(target)
                    finish()
                }
                is AuthViewModel.AuthState.Error -> {
                    binding.btnSignup.isEnabled = true
                    binding.btnSignup.text = "REGISTER PROTOCOL"
                    binding.cvSignupForm.shake()
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
}
