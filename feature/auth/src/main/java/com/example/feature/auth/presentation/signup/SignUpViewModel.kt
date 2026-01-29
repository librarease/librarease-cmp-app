package com.example.feature.auth.presentation.signup

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

    fun signUp(name: String, email: String, password: String) {
        val nameValidation = AuthValidator.validateName(name)
        if (!nameValidation.isValid) {
            _uiState.value = SignUpUiState.Error(nameValidation.errorMessage ?: "Invalid name")
            return
        }

        val emailValidation = AuthValidator.validateEmail(email)
        if (!emailValidation.isValid) {
            _uiState.value = SignUpUiState.Error(emailValidation.errorMessage ?: "Invalid email")
            return
        }

        val passwordValidation = AuthValidator.validatePassword(password)
        if (!passwordValidation.isValid) {
            _uiState.value = SignUpUiState.Error(passwordValidation.errorMessage ?: "Invalid password")
            return
        }

        _uiState.value = SignUpUiState.Loading

        viewModelScope.launch {
            when (val result = signUpUseCase(email, password, name)) {
                is AuthResult.Success -> {
                    _uiState.value = SignUpUiState.Success
                }
                is AuthResult.Error -> {
                    _uiState.value = SignUpUiState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    _uiState.value = SignUpUiState.Loading
                }
            }
        }
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
