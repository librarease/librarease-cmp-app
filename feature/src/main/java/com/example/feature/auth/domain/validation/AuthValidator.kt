package com.example.feature.auth.domain.validation

object AuthValidator {
    
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid("Email cannot be empty")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationResult.Invalid("Invalid email format")
            else -> ValidationResult.Valid
        }
    }
    
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid("Password cannot be empty")
            password.length < 8 -> ValidationResult.Invalid("Password must be at least 8 characters")
            !password.any { it.isDigit() } -> ValidationResult.Invalid("Password must contain at least one digit")
            !password.any { it.isUpperCase() } -> ValidationResult.Invalid("Password must contain at least one uppercase letter")
            else -> ValidationResult.Valid
        }
    }
    
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("Name cannot be empty")
            name.length < 2 -> ValidationResult.Invalid("Name must be at least 2 characters")
            else -> ValidationResult.Valid
        }
    }
}

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}
