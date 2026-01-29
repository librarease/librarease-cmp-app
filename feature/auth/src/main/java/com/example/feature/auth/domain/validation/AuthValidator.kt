package com.example.feature.auth.domain.validation

object AuthValidator {
    
    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "Email cannot be empty")
        }
        
        if (!ValidationRegex.EMAIL_REGEX.matches(email)) {
            return ValidationResult(false, "Invalid email format")
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
        
        if (!ValidationRegex.PASSWORD_REGEX.matches(password)) {
            return ValidationResult(
                false, 
                "Password must contain uppercase, lowercase, number and special character"
            )
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
