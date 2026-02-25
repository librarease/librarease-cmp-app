package com.example.feature.auth.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.core.storage.authPrefsDataStore
import com.example.feature.auth.data.model.SignUpRequest
import com.example.feature.auth.data.model.toUserDomain
import com.example.feature.auth.data.remote.AuthApiService
import com.example.feature.auth.domain.model.AuthErrorCode
import com.example.feature.auth.domain.model.AuthResult
import com.example.feature.auth.domain.model.User
import com.example.feature.auth.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val firebaseAuth: FirebaseAuth,
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
        private val USER_KEY = stringPreferencesKey("user_data")
    }

    override suspend fun signUp(email: String, password: String, name: String): AuthResult<User> {
        return try {

            if (!isValidEmail(email)) {
                return AuthResult.Error("Invalid email format", AuthErrorCode.INVALID_EMAIL)
            }
            
            if (!isValidPassword(password)) {
                return AuthResult.Error(
                    "Password must be at least 8 characters",
                    AuthErrorCode.WEAK_PASSWORD
                )
            }

            val request = SignUpRequest(email = email, password = password, name = name)
            val response = authApiService.signUp(request)

            val user = response.toUserDomain()
            saveUser(user)
            
            AuthResult.Success(user)
        } catch (e: Exception) {
            handleAuthException(e)
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult<User> {
        return try {
            val trimmedEmail = email.trim()
            if (!isValidEmail(trimmedEmail)) {
                return AuthResult.Error("Invalid email format", AuthErrorCode.INVALID_EMAIL)
            }

            val authResult = firebaseAuth
                .signInWithEmailAndPassword(trimmedEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: return AuthResult.Error("Sign in failed", AuthErrorCode.UNKNOWN_ERROR)

            val user = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName.orEmpty(),
                email = firebaseUser.email ?: trimmedEmail,
                createdAt = "",
                updatedAt = ""
            )

            saveUser(user)
            AuthResult.Success(user)
        } catch (e: Exception) {
            handleSignInException(e)
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            firebaseAuth.signOut()
            clearUser()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error("Failed to sign out", AuthErrorCode.UNKNOWN_ERROR)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val userJson = context.authPrefsDataStore.data.first()[USER_KEY]
            userJson?.let { json.decodeFromString<User>(it) }
        } catch (e: Exception) {
            null
        }
    }

    override fun isUserLoggedIn(): Flow<Boolean> {
        return context.authPrefsDataStore.data.map { preferences ->
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
        context.authPrefsDataStore.edit { preferences ->
            preferences[USER_KEY] = json.encodeToString(User.serializer(), user)
        }
    }

    private suspend fun clearUser() {
        context.authPrefsDataStore.edit { preferences ->
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

    private fun handleSignInException(exception: Exception): AuthResult.Error {
        return when (exception) {
            is FirebaseAuthInvalidUserException ->
                AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND)
            is FirebaseAuthInvalidCredentialsException ->
                AuthResult.Error("Invalid email or password", AuthErrorCode.INVALID_PASSWORD)
            is FirebaseTooManyRequestsException ->
                AuthResult.Error("Too many attempts. Try again later.", AuthErrorCode.UNKNOWN_ERROR)
            is FirebaseNetworkException ->
                AuthResult.Error("Network error. Please check your connection", AuthErrorCode.NETWORK_ERROR)
            is FirebaseAuthUserCollisionException ->
                AuthResult.Error("Email already exists", AuthErrorCode.EMAIL_ALREADY_EXISTS)
            else ->
                AuthResult.Error(exception.message ?: "Unknown error occurred", AuthErrorCode.UNKNOWN_ERROR)
        }
    }
}
