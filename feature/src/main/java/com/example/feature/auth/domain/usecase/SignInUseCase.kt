package com.example.feature.auth.domain.usecase

import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.model.User
import com.example.feature.auth.domain.repository.AuthRepository

class SignInUseCase(private val authRepository: AuthRepository) {
    
    suspend operator fun invoke(email: String, password: String): AuthResult<User> {
        if (email.isBlank()) {
            return AuthResult.Error("Email cannot be empty")
        }
        
        if (password.isBlank()) {
            return AuthResult.Error("Password cannot be empty")
        }
        
        return authRepository.signIn(email.trim(), password)
    }
}
