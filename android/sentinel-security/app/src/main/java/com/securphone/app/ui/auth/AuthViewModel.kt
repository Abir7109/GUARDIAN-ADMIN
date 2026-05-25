package com.securphone.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.securphone.app.data.firebase.FirebaseManager
import com.securphone.app.utils.Constants
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Please enter email and password")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = FirebaseManager.signInWithEmail(email, password)
            result.fold(
                onSuccess = { user ->
                    val userDoc = FirebaseManager.getUserDocument(user.uid)
                    userDoc.fold(
                        onSuccess = { userModel ->
                            if (userModel.isBlocked) {
                                FirebaseManager.signOut()
                                _authState.value = AuthState.Error("Your account has been blocked")
                            } else {
                                FirebaseManager.updateLastActive(user.uid)
                                _authState.value = AuthState.Success
                            }
                        },
                        onFailure = { _authState.value = AuthState.Success }
                    )
                },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun signUp(email: String, password: String, name: String, phone: String = "") {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            _authState.value = AuthState.Error("Please fill all fields")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = FirebaseManager.signUpWithEmail(email, password)
            result.fold(
                onSuccess = { user ->
                    val userModel = com.securphone.app.data.models.UserModel(
                        id = user.uid,
                        email = email,
                        displayName = name,
                        phone = phone,
                        appVersion = Constants.CURRENT_VERSION,
                        createdAt = System.currentTimeMillis(),
                        lastLogin = System.currentTimeMillis()
                    )
                    FirebaseManager.createUserDocument(userModel)
                    val token = FirebaseManager.getFcmToken().getOrNull()
                    if (token != null) FirebaseManager.updateFcmToken(user.uid, token)
                    _authState.value = AuthState.Success
                },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Signup failed") }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = FirebaseManager.signInWithGoogle(idToken)
            result.fold(
                onSuccess = { user ->
                    val userDoc = FirebaseManager.getUserDocument(user.uid)
                    userDoc.fold(
                        onSuccess = { userModel ->
                            if (userModel.isBlocked) {
                                FirebaseManager.signOut()
                                _authState.value = AuthState.Error("Your account has been blocked")
                            } else {
                                FirebaseManager.updateLastActive(user.uid)
                                val token = FirebaseManager.getFcmToken().getOrNull()
                                if (token != null) FirebaseManager.updateFcmToken(user.uid, token)
                                _authState.value = AuthState.Success
                            }
                        },
                        onFailure = {
                            val newUser = com.securphone.app.data.models.UserModel(
                                id = user.uid,
                                email = user.email ?: "",
                                displayName = user.displayName ?: "",
                                appVersion = Constants.CURRENT_VERSION,
                                createdAt = System.currentTimeMillis(),
                                lastLogin = System.currentTimeMillis()
                            )
                            FirebaseManager.createUserDocument(newUser)
                            val token = FirebaseManager.getFcmToken().getOrNull()
                            if (token != null) FirebaseManager.updateFcmToken(user.uid, token)
                            _authState.value = AuthState.Success
                        }
                    )
                },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Google sign-in failed") }
            )
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
