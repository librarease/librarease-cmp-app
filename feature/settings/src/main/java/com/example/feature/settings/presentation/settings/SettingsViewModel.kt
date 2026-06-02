package com.example.feature.settings.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.model.AuthErrorCode
import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.model.User
import com.example.feature.auth.domain.usecase.ChangePasswordUseCase
import com.example.feature.auth.domain.usecase.DeleteAccountUseCase
import com.example.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.feature.auth.domain.usecase.SignOutUseCase
import com.example.feature.auth.domain.validation.AuthValidator
import com.example.feature.settings.data.SettingsPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val settingsPreferencesRepository: SettingsPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
        loadCurrentUser()
    }

    fun onPushNotificationsChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferencesRepository.setPushNotificationsEnabled(enabled)
        }
    }

    fun onDueDateRemindersChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferencesRepository.setDueDateRemindersEnabled(enabled)
        }
    }

    fun changePassword(newPassword: String, confirmPassword: String) {
        val normalizedPassword = newPassword.trim()
        val normalizedConfirmation = confirmPassword.trim()
        val validation = AuthValidator.validatePassword(normalizedPassword)

        if (!validation.isValid) {
            showError(validation.errorMessage ?: "Password must be at least 8 characters")
            return
        }

        if (normalizedPassword != normalizedConfirmation) {
            showError("Passwords do not match")
            return
        }

        _uiState.update { state ->
            state.copy(isWorking = true, feedbackMessage = null, feedbackError = null)
        }

        viewModelScope.launch {
            when (val result = changePasswordUseCase(normalizedPassword)) {
                is AuthResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isWorking = false,
                            feedbackMessage = "Password updated successfully."
                        )
                    }
                }

                is AuthResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isWorking = false,
                            feedbackError = mapPasswordError(result)
                        )
                    }
                }

                is AuthResult.Loading -> {
                    _uiState.update { state -> state.copy(isWorking = true) }
                }
            }
        }
    }

    fun signOut() {
        _uiState.update { state ->
            state.copy(isWorking = true, feedbackMessage = null, feedbackError = null)
        }

        viewModelScope.launch {
            when (val result = signOutUseCase()) {
                is AuthResult.Success -> {
                    _uiState.update { state ->
                        state.copy(isWorking = false, shouldExitToSignIn = true)
                    }
                }

                is AuthResult.Error -> {
                    _uiState.update { state ->
                        state.copy(isWorking = false, feedbackError = result.message)
                    }
                }

                is AuthResult.Loading -> {
                    _uiState.update { state -> state.copy(isWorking = true) }
                }
            }
        }
    }

    fun deleteAccount() {
        _uiState.update { state ->
            state.copy(isWorking = true, feedbackMessage = null, feedbackError = null)
        }

        viewModelScope.launch {
            when (val result = deleteAccountUseCase()) {
                is AuthResult.Success -> {
                    _uiState.update { state ->
                        state.copy(isWorking = false, shouldExitToSignIn = true)
                    }
                }

                is AuthResult.Error -> {
                    _uiState.update { state ->
                        state.copy(isWorking = false, feedbackError = mapDeleteAccountError(result))
                    }
                }

                is AuthResult.Loading -> {
                    _uiState.update { state -> state.copy(isWorking = true) }
                }
            }
        }
    }

    fun consumeFeedback() {
        _uiState.update { state ->
            state.copy(feedbackMessage = null, feedbackError = null)
        }
    }

    fun consumeExitNavigation() {
        _uiState.update { state ->
            state.copy(shouldExitToSignIn = false)
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            settingsPreferencesRepository.observePreferences().collect { preferences ->
                _uiState.update { state ->
                    state.copy(
                        pushNotificationsEnabled = preferences.pushNotificationsEnabled,
                        dueDateRemindersEnabled = preferences.dueDateRemindersEnabled
                    )
                }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            _uiState.update { state ->
                state.copy(user = user, isLoadingUser = false)
            }
        }
    }

    private fun showError(message: String) {
        _uiState.update { state ->
            state.copy(feedbackError = message)
        }
    }

    private fun mapPasswordError(error: AuthResult.Error): String {
        return when (error.code) {
            AuthErrorCode.WEAK_PASSWORD -> "Password must be at least 8 characters"
            AuthErrorCode.USER_NOT_FOUND -> "Please sign in again before changing your password."
            AuthErrorCode.NETWORK_ERROR -> "Network error. Check your connection and try again."
            else -> error.message
        }
    }

    private fun mapDeleteAccountError(error: AuthResult.Error): String {
        return when (error.code) {
            AuthErrorCode.USER_NOT_FOUND -> "Please sign in again before deleting your account."
            AuthErrorCode.NETWORK_ERROR -> "Network error. Check your connection and try again."
            else -> error.message
        }
    }
}

data class SettingsUiState(
    val user: User? = null,
    val isLoadingUser: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val dueDateRemindersEnabled: Boolean = true,
    val isWorking: Boolean = false,
    val feedbackMessage: String? = null,
    val feedbackError: String? = null,
    val shouldExitToSignIn: Boolean = false
)
