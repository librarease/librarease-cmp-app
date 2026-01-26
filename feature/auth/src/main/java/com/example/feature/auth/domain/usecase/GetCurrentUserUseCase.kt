package com.example.feature.auth.domain.usecase

import com.example.feature.auth.domain.model.User
import com.example.feature.auth.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}
