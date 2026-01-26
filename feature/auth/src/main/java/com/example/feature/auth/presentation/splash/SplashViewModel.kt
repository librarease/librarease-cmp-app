package com.example.feature.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.usecase.IsUserLoggedInUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {

    fun checkAuthState(
        onAuthenticated: () -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase().first()
            if (isLoggedIn) {
                onAuthenticated()
            } else {
                onUnauthenticated()
            }
        }
    }
}
