package com.example.feature.auth.domain.validation

import android.util.Patterns

object AuthValidator {
    
    fun validateEmail(email: String): ValidationResult {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) {
            return ValidationResult(false, "Email cannot be empty")
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return ValidationResult(false, "Please enter a valid email address")
        }
        
        return ValidationResult(true)
    }
    
    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(false, "Password cannot be empty")
        }
        
        if (password.length < 8) {
            return ValidationResult(false, "Password must be at least 8 characters")
        }
        
        return ValidationResult(true)
    }
    
    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "Name cannot be empty")
        }
        
        if (name.length < 2) {
            return ValidationResult(false, "Name must be at least 2 characters")
        }
        
        return ValidationResult(true)
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
