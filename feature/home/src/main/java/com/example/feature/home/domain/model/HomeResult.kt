package com.example.feature.home.domain.model

sealed class HomeResult<out T> {
    data class Success<T>(val data: T) : HomeResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : HomeResult<Nothing>()
    data object Loading : HomeResult<Nothing>()
}
