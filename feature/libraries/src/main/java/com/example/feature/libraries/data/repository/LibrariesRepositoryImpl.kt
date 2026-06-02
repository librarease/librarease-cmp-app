package com.example.feature.libraries.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.core.model.book.LibraryInfo
import com.example.core.storage.authPrefsDataStore
import com.example.feature.libraries.data.model.LibraryItemDto
import com.example.feature.libraries.data.model.toDomain
import com.example.feature.libraries.data.model.toDto
import com.example.feature.libraries.data.remote.LibrariesApiService
import com.example.feature.libraries.domain.model.LibrariesResult
import com.example.feature.libraries.domain.repository.LibrariesRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

class LibrariesRepositoryImpl(
    private val librariesApiService: LibrariesApiService,
    private val firebaseAuth: FirebaseAuth,
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) : LibrariesRepository {

    override suspend fun getCachedLibraries(): List<LibraryInfo> {
        val preferences = context.authPrefsDataStore.data.first()
        val cachedPayload = preferences[LATEST_LIBRARIES_CACHE_KEY] ?: return emptyList()
        return decodeCachedLibraries(cachedPayload)
    }

    override suspend fun getLibraries(limit: Int?): LibrariesResult<List<LibraryInfo>> {
        val authToken = resolveAuthToken()

        if (!authToken.isNullOrBlank()) {
            try {
                val dtos = librariesApiService.getLibraries(limit, authToken)
                cacheLibraries(dtos)
                return LibrariesResult.Success(dtos.map { dto -> dto.toDomain() })
            } catch (e: Exception) {
                Log.w(TAG, "Libraries call failed with token", e)
            }
        }

        return try {
            val dtos = librariesApiService.getLibraries(limit, null)
            cacheLibraries(dtos)
            LibrariesResult.Success(dtos.map { dto -> dto.toDomain() })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load libraries", e)
            LibrariesResult.Error(e.message ?: "Failed to load libraries", e)
        }
    }

    private suspend fun cacheLibraries(libraries: List<LibraryItemDto>) {
        val encoded = json.encodeToString(libraries)
        context.authPrefsDataStore.edit { preferences ->
            preferences[LATEST_LIBRARIES_CACHE_KEY] = encoded
        }
    }

    private fun decodeCachedLibraries(payload: String): List<LibraryInfo> {
        return try {
            json.decodeFromString<List<LibraryItemDto>>(payload).map { dto -> dto.toDomain() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode cached libraries", e)
            emptyList()
        }
    }

    private suspend fun resolveAuthToken(): String? {
        val currentUser = firebaseAuth.currentUser ?: return null
        return try {
            currentUser.getIdToken(false).await().token
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve Firebase auth token", e)
            null
        }
    }

    companion object {
        private const val TAG = "LibrariesRepository"
        private val LATEST_LIBRARIES_CACHE_KEY = stringPreferencesKey("libraries_cache_latest")
    }
}
