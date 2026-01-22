package com.example.feature.auth.domain.usecase

import com.example.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class IsUserLoggedInUseCase(private val authRepository: AuthRepository) {
    
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isUserLoggedIn()
    }
}
