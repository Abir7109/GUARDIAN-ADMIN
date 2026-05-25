package com.securphone.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.securphone.app.data.preferences.PreferencesManager
import com.securphone.app.databinding.ActivityLoginBinding
import com.securphone.app.ui.main.MainActivity
import com.securphone.app.ui.setup.SetupWizardActivity
import com.securphone.app.utils.Constants
import com.securphone.app.utils.shake

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            account?.idToken?.let { viewModel.signInWithGoogle(it) }
        } catch (e: Exception) {
            Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTabSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding.btnAuthenticate.setOnClickListener {
            val email = binding.etOperatorId.text.toString().trim()
            val code = binding.etAccessCode.text.toString().trim()
            viewModel.login(email, code)
        }

        binding.btnSsoGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.btnAuthenticate.isEnabled = false
                    binding.btnAuthenticate.text = "AUTHENTICATING..."
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
                    binding.btnAuthenticate.isEnabled = true
                    binding.btnAuthenticate.text = "AUTHENTICATE"
                    binding.cvLoginForm.shake()
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
}
