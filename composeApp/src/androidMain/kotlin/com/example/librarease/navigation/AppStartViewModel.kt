package com.example.librarease.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.feature.auth.domain.usecase.IsUserLoggedInUseCase
import com.example.feature.home.domain.usecase.WarmUpHomeUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class StartDestination {
    HOME,
    AUTH
}

class AppStartViewModel(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val warmUpHomeUseCase: WarmUpHomeUseCase
) : ViewModel() {

    private val _destination = MutableStateFlow<StartDestination?>(null)
    val destination: StateFlow<StartDestination?> = _destination.asStateFlow()
    private var started = false

    fun start() {
        if (started) return
        started = true

        viewModelScope.launch {
            delay(2000)
            val isLoggedIn = isUserLoggedInUseCase().first()
            if (isLoggedIn) {
                val userId = getCurrentUserUseCase()?.id
                withTimeoutOrNull(1500) {
                    warmUpHomeUseCase(userId)
                }
                _destination.value = StartDestination.HOME
            } else {
                _destination.value = StartDestination.AUTH
            }
        }
    }

    fun consumeDestination() {
        _destination.value = null
    }
}
