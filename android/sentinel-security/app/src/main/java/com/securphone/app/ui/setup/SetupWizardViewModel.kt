package com.securphone.app.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SetupWizardViewModel : ViewModel() {

    private val _currentStep = MutableLiveData(1)
    val currentStep: LiveData<Int> = _currentStep

    private val _securedCount = MutableLiveData(0)
    val securedCount: LiveData<Int> = _securedCount

    fun nextStep() {
        val next = (_currentStep.value ?: 1) + 1
        if (next <= 6) {
            _currentStep.value = next
            _securedCount.value = _securedCount.value?.plus(1) ?: 1
        }
    }

    fun skipStep() {
        val next = (_currentStep.value ?: 1) + 1
        if (next <= 6) {
            _currentStep.value = next
        }
    }
}
