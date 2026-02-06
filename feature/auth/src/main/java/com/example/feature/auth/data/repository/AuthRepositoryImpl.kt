package com.example.feature.auth.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.feature.auth.data.model.SignInRequest
import com.example.feature.auth.data.model.SignUpRequest
import com.example.feature.auth.data.model.toUserDomain
import com.example.feature.auth.data.remote.AuthApiService
import com.example.feature.auth.domain.model.AuthErrorCode
import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.model.User
import com.example.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
        private val USER_KEY = stringPreferencesKey("user_data")
    }

    override suspend fun signUp(email: String, password: String, name: String): AuthResult<User> {
        return try {
            Log.d(TAG, "SignUp started for email: $email, name: $name")
            
            if (!isValidEmail(email)) {
                Log.d(TAG, "SignUp failed: Invalid email format")
                return AuthResult.Error("Invalid email format", AuthErrorCode.INVALID_EMAIL)
            }
            
            if (!isValidPassword(password)) {
                Log.d(TAG, "SignUp failed: Weak password")
                return AuthResult.Error(
                    "Password must be at least 8 characters",
                    AuthErrorCode.WEAK_PASSWORD
                )
            }

            val request = SignUpRequest(email = email, password = password, name = name)
            Log.d(TAG, "Making API call to sign up...")
            val response = authApiService.signUp(request)
            Log.d(TAG, "API call successful, response received")
            
            val user = response.toUserDomain()
            saveUser(user)
            
            Log.d(TAG, "SignUp successful for user: ${user.email}")
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "SignUp failed with exception: ${e.message}", e)
            handleAuthException(e)
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult<User> {
        return try {
            if (!isValidEmail(email)) {
                return AuthResult.Error("Invalid email format", AuthErrorCode.INVALID_EMAIL)
            }

            val request = SignInRequest(email = email, password = password)
            val response = authApiService.signIn(request)
            val user = response.toUserDomain()
            
            saveUser(user)
            
            AuthResult.Success(user)
        } catch (e: Exception) {
            handleAuthException(e)
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            clearUser()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error("Failed to sign out", AuthErrorCode.UNKNOWN_ERROR)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val userJson = context.dataStore.data.first()[USER_KEY]
            userJson?.let { json.decodeFromString<User>(it) }
        } catch (e: Exception) {
            null
        }
    }

    override fun isUserLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_KEY] != null
        }
    }

    override suspend fun saveAuthToken(token: String) {

    }

    override suspend fun getAuthToken(): String? {
        return null
    }

    override suspend fun clearAuthToken() {

    }

    private suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = json.encodeToString(User.serializer(), user)
        }
    }

    private suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_KEY)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun handleAuthException(exception: Exception): AuthResult.Error {
        return when {
            exception.message?.contains("404") == true -> 
                AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND)
            exception.message?.contains("409") == true -> 
                AuthResult.Error("Email already exists", AuthErrorCode.EMAIL_ALREADY_EXISTS)
            exception.message?.contains("network", ignoreCase = true) == true -> 
                AuthResult.Error("Network error. Please check your connection", AuthErrorCode.NETWORK_ERROR)
            else -> 
                AuthResult.Error(exception.message ?: "Unknown error occurred", AuthErrorCode.UNKNOWN_ERROR)
        }
    }
}
