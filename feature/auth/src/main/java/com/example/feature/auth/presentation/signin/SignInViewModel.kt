package com.example.feature.auth.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.usecase.SignInUseCase
import com.example.feature.auth.domain.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        val emailValidation = AuthValidator.validateEmail(email)
        if (!emailValidation.isValid) {
            _uiState.value = SignInUiState.Error(emailValidation.errorMessage ?: "Invalid email")
            return
        }

        val passwordValidation = AuthValidator.validatePassword(password)
        if (!passwordValidation.isValid) {
            _uiState.value = SignInUiState.Error(passwordValidation.errorMessage ?: "Invalid password")
            return
        }

        _uiState.value = SignInUiState.Loading

        viewModelScope.launch {
            when (val result = signInUseCase(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = SignInUiState.Success
                }
                is AuthResult.Error -> {
                    _uiState.value = SignInUiState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    _uiState.value = SignInUiState.Loading
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = SignInUiState.Idle
    }
}

sealed class SignInUiState {
    data object Idle : SignInUiState()
    data object Loading : SignInUiState()
    data object Success : SignInUiState()
    data class Error(val message: String) : SignInUiState()
}
