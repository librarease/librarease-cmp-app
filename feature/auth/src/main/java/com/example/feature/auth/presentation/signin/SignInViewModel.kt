package com.example.feature.auth.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.model.AuthErrorCode
import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.usecase.SignInUseCase
import com.example.feature.auth.domain.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        val normalizedEmail = email.trim()
        val emailValidation = AuthValidator.validateEmail(normalizedEmail)
        val passwordValidation = AuthValidator.validatePassword(password)

        val hasEmailError = !emailValidation.isValid
        val hasPasswordError = !passwordValidation.isValid

        if (hasEmailError || hasPasswordError) {
            _uiState.value = SignInUiState(
                emailError = emailValidation.errorMessage.takeIf { hasEmailError },
                passwordError = passwordValidation.errorMessage.takeIf { hasPasswordError }
            )
            return
        }

        _uiState.value = SignInUiState(isLoading = true)

        viewModelScope.launch {
            when (val result = signInUseCase(normalizedEmail, password)) {
                is AuthResult.Success -> {
                    _uiState.value = SignInUiState(isSuccess = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = mapErrorToUiState(result)
                }
                is AuthResult.Loading -> {
                    _uiState.value = SignInUiState(isLoading = true)
                }
            }
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

    private fun mapErrorToUiState(error: AuthResult.Error): SignInUiState {
        return when (error.code) {
            AuthErrorCode.INVALID_EMAIL -> SignInUiState(
                emailError = "Please enter a valid email address"
            )
            AuthErrorCode.USER_NOT_FOUND -> SignInUiState(
                emailError = "No account found for this email"
            )
            AuthErrorCode.INVALID_PASSWORD -> SignInUiState(
                passwordError = "Incorrect password. Try again or reset it."
            )
            AuthErrorCode.NETWORK_ERROR -> SignInUiState(
                generalError = "Network error. Check your connection and try again."
            )
            else -> SignInUiState(
                generalError = error.message
            )
        }
    }
}

data class SignInUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)
