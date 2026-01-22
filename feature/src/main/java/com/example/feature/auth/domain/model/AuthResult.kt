package com.example.feature.auth.domain.model

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val code: AuthErrorCode? = null) : AuthResult<Nothing>()
    data object Loading : AuthResult<Nothing>()
}

enum class AuthErrorCode {
    INVALID_EMAIL,
    INVALID_PASSWORD,
    USER_NOT_FOUND,
    EMAIL_ALREADY_EXISTS,
    WEAK_PASSWORD,
    NETWORK_ERROR,
    UNKNOWN_ERROR
}
