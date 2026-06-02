package com.example.feature.auth.domain.usecase

import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.repository.AuthRepository

class DeleteAccountUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(): AuthResult<Unit> {
        return authRepository.deleteAccount()
    }
}
