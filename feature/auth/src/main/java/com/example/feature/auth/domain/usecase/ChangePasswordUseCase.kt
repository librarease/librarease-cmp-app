package com.example.feature.auth.domain.usecase

import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.repository.AuthRepository

class ChangePasswordUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(newPassword: String): AuthResult<Unit> {
        return authRepository.changePassword(newPassword)
    }
}
