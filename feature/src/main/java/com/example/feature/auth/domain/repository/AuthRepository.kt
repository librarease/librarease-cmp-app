package com.example.feature.auth.domain.repository

import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String, name: String): AuthResult<User>
    suspend fun signIn(email: String, password: String): AuthResult<User>
    suspend fun signOut(): AuthResult<Unit>
    suspend fun getCurrentUser(): User?
    fun isUserLoggedIn(): Flow<Boolean>
    suspend fun saveAuthToken(token: String)
    suspend fun getAuthToken(): String?
    suspend fun clearAuthToken()
}
