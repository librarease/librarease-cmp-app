package com.example.feature.auth.presentation.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.usecase.SignUpUseCase
import com.example.feature.auth.domain.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "SignUpViewModel created")
    }

    fun signUp(name: String, email: String, password: String) {
        Log.d(TAG, "signUp called with name='$name', email='$email'")
        
        val nameValidation = AuthValidator.validateName(name)
        if (!nameValidation.isValid) {
            Log.d(TAG, "Name validation failed: ${nameValidation.errorMessage}")
            _uiState.value = SignUpUiState.Error(nameValidation.errorMessage ?: "Invalid name")
            return
        }

        val emailValidation = AuthValidator.validateEmail(email)
        if (!emailValidation.isValid) {
            Log.d(TAG, "Email validation failed: ${emailValidation.errorMessage}")
            _uiState.value = SignUpUiState.Error(emailValidation.errorMessage ?: "Invalid email")
            return
        }

        val passwordValidation = AuthValidator.validatePassword(password)
        if (!passwordValidation.isValid) {
            Log.d(TAG, "Password validation failed: ${passwordValidation.errorMessage}")
            _uiState.value = SignUpUiState.Error(passwordValidation.errorMessage ?: "Invalid password")
            return
        }

        Log.d(TAG, "All validations passed, setting Loading state")
        _uiState.value = SignUpUiState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Calling signUpUseCase...")
            when (val result = signUpUseCase(email, password, name)) {
                is AuthResult.Success -> {
                    Log.d(TAG, "SignUp successful!")
                    _uiState.value = SignUpUiState.Success
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "SignUp error: ${result.message}")
                    _uiState.value = SignUpUiState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    Log.d(TAG, "SignUp still loading")
                    _uiState.value = SignUpUiState.Loading
                }
            }
        }
    }

    companion object {
        private const val TAG = "SignUpViewModel"
    }

    fun resetState() {
        _uiState.value = SignUpUiState.Idle
    }
}

sealed class SignUpUiState {
    data object Idle : SignUpUiState()
    data object Loading : SignUpUiState()
    data object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}
