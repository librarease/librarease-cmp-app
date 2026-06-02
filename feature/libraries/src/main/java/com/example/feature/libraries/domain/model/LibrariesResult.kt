package com.example.feature.libraries.domain.model

sealed class LibrariesResult<out T> {
    data class Success<T>(val data: T) : LibrariesResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : LibrariesResult<Nothing>()
    data object Loading : LibrariesResult<Nothing>()
}
