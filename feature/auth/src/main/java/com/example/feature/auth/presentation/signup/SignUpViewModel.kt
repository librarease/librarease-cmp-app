package com.example.feature.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.model.AuthErrorCode
import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.usecase.SignUpUseCase
import com.example.feature.auth.domain.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun signUp(name: String, email: String, password: String) {
        val normalizedName = name.trim()
        val normalizedEmail = email.trim()

        val nameValidation = AuthValidator.validateName(normalizedName)
        val emailValidation = AuthValidator.validateEmail(normalizedEmail)
        val passwordValidation = AuthValidator.validatePassword(password)

        val hasNameError = !nameValidation.isValid
        val hasEmailError = !emailValidation.isValid
        val hasPasswordError = !passwordValidation.isValid

        if (hasNameError || hasEmailError || hasPasswordError) {
            _uiState.value = SignUpUiState(
                nameError = nameValidation.errorMessage.takeIf { hasNameError },
                emailError = emailValidation.errorMessage.takeIf { hasEmailError },
                passwordError = passwordValidation.errorMessage.takeIf { hasPasswordError }
            )
            return
        }

        _uiState.value = SignUpUiState(isLoading = true)

        viewModelScope.launch {
            when (val result = signUpUseCase(normalizedEmail, password, normalizedName)) {
                is AuthResult.Success -> {
                    _uiState.value = SignUpUiState(isSuccess = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = mapErrorToUiState(result)
                }
                is AuthResult.Loading -> {
                    _uiState.value = SignUpUiState(isLoading = true)
                }
            }
        }
    }

    fun onNameChanged() {
        _uiState.update { state ->
            state.copy(nameError = null, generalError = null)
        }
    }

    fun onEmailChanged() {
        _uiState.update { state ->
            state.copy(emailError = null, generalError = null)
        }
    }

    fun onPasswordChanged() {
        _uiState.update { state ->
            state.copy(passwordError = null, generalError = null)
        }
    }

    fun consumeSuccess() {
        _uiState.update { state ->
            state.copy(isSuccess = false)
        }
    }

    private fun mapErrorToUiState(error: AuthResult.Error): SignUpUiState {
        return when (error.code) {
            AuthErrorCode.INVALID_EMAIL -> SignUpUiState(
                emailError = "Please enter a valid email address"
            )
            AuthErrorCode.EMAIL_ALREADY_EXISTS -> SignUpUiState(
                emailError = "An account already exists for this email"
            )
            AuthErrorCode.WEAK_PASSWORD -> SignUpUiState(
                passwordError = "Password must be at least 8 characters"
            )
            AuthErrorCode.NETWORK_ERROR -> SignUpUiState(
                generalError = "Network error. Check your connection and try again."
            )
            else -> SignUpUiState(
                generalError = error.message
            )
        }
    }
}

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)
