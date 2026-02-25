package com.example.feature.home.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.core.storage.authPrefsDataStore
import com.example.feature.home.data.model.BorrowingDto
import com.example.feature.home.data.model.toDomain
import com.example.feature.home.data.remote.HomeApiService
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.model.Membership
import com.example.feature.home.domain.repository.HomeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HomeRepositoryImpl(
    private val homeApiService: HomeApiService,
    private val firebaseAuth: FirebaseAuth,
    private val context: Context,
    private val json: Json
) : HomeRepository {

    override suspend fun getCachedBorrowings(userId: String?): List<Borrowing> {
        val userIdCandidates = resolveUserIdCandidates(userId)

        val preferences = context.authPrefsDataStore.data.first()
        val latestCachedPayload = preferences[LATEST_BORROWINGS_CACHE_KEY]
        if (!latestCachedPayload.isNullOrBlank()) {
            val latestDecoded = decodeCachedBorrowings(latestCachedPayload, "latest")
            if (latestDecoded.isNotEmpty()) {
                return latestDecoded
            }
        }

        if (userIdCandidates.isEmpty()) return emptyList()

        userIdCandidates.forEach { candidateUserId ->
            val cacheKey = borrowingsCacheKey(candidateUserId)
            val cachedPayload = preferences[cacheKey] ?: return@forEach
            val decoded = decodeCachedBorrowings(cachedPayload, candidateUserId)
            if (decoded.isNotEmpty()) {
                return decoded
            }
        }

        return emptyList()
    }

    override suspend fun getBorrowings(userId: String?): HomeResult<List<Borrowing>> {
        val userIdCandidates = resolveUserIdCandidates(userId)
        if (userIdCandidates.isEmpty()) {
            return HomeResult.Error("No Firebase user id available. Please sign in first.")
        }

        val authToken = resolveAuthToken()
        var lastError: Exception? = null

        userIdCandidates.forEach { candidateUserId ->
            Log.d(TAG, "Trying borrowings with userId=$candidateUserId")
            if (!authToken.isNullOrBlank()) {
                try {
                    val borrowingDtosWithToken = homeApiService.getBorrowings(
                        userId = candidateUserId,
                        authToken = authToken
                    )
                    cacheBorrowings(candidateUserId, borrowingDtosWithToken)
                    val borrowingsWithToken = borrowingDtosWithToken.map { dto ->
                        dto.toDomain()
                    }
                    return HomeResult.Success(borrowingsWithToken)
                } catch (e: Exception) {
                    lastError = e
                    Log.w(TAG, "Borrowings call failed with token for userId=$candidateUserId", e)
                }
            }

            try {
                val borrowingDtosWithoutToken = homeApiService.getBorrowings(userId = candidateUserId)
                cacheBorrowings(candidateUserId, borrowingDtosWithoutToken)
                val borrowingsWithoutToken = borrowingDtosWithoutToken.map { dto ->
                    dto.toDomain()
                }
                return HomeResult.Success(borrowingsWithoutToken)
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "Borrowings call failed without token for userId=$candidateUserId", e)
            }
        }

        Log.e(TAG, "Failed to load borrowings for all user id candidates", lastError)
        return HomeResult.Error(
            message = lastError?.message ?: "Failed to load borrowings",
            cause = lastError
        )
    }

    override suspend fun getBorrowingById(
        borrowingId: String,
        userId: String?
    ): HomeResult<Borrowing> {
        val cachedBorrowing = getCachedBorrowings(userId).firstOrNull { borrowing ->
            borrowing.id == borrowingId
        }
        if (cachedBorrowing != null) {
            return HomeResult.Success(cachedBorrowing)
        }

        return when (val borrowingsResult = getBorrowings(userId)) {
            is HomeResult.Success -> {
                val borrowing = borrowingsResult.data.firstOrNull { item ->
                    item.id == borrowingId
                }
                if (borrowing != null) {
                    HomeResult.Success(borrowing)
                } else {
                    HomeResult.Error("Borrowing not found")
                }
            }
            is HomeResult.Error -> HomeResult.Error(borrowingsResult.message, borrowingsResult.cause)
            is HomeResult.Loading -> HomeResult.Loading
        }
    }

    override suspend fun getMemberships(): HomeResult<List<Membership>> {
        return HomeResult.Success(emptyList())
    }

    companion object {
        private const val TAG = "HomeRepository"
        private val USER_KEY = stringPreferencesKey("user_data")
        private val LATEST_BORROWINGS_CACHE_KEY = stringPreferencesKey("borrowings_cache_latest")
    }

    private suspend fun resolveUserIdCandidates(explicitUserId: String?): List<String> {
        val ids = linkedSetOf<String>()
        firebaseAuth.currentUser?.uid?.let(ids::add)
        explicitUserId?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(ids::add)
        getStoredUserId()?.let(ids::add)
        return ids.toList()
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

    private suspend fun getStoredUserId(): String? {
        val rawUser = context.authPrefsDataStore.data.first()[USER_KEY] ?: return null
        return try {
            val userJson = json.parseToJsonElement(rawUser).jsonObject
            userJson["id"]?.jsonPrimitive?.contentOrNull
                ?: userJson["userId"]?.jsonPrimitive?.contentOrNull
                ?: userJson["uid"]?.jsonPrimitive?.contentOrNull
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse stored user id", e)
            null
        }
    }

    private suspend fun cacheBorrowings(
        userId: String,
        borrowings: List<BorrowingDto>
    ) {
        val cacheKey = borrowingsCacheKey(userId)
        val encoded = json.encodeToString(borrowings)
        context.authPrefsDataStore.edit { preferences ->
            preferences[cacheKey] = encoded
            preferences[LATEST_BORROWINGS_CACHE_KEY] = encoded
        }
    }

    private fun borrowingsCacheKey(userId: String) = stringPreferencesKey("borrowings_cache_$userId")

    private fun decodeCachedBorrowings(
        cachedPayload: String,
        userIdTag: String
    ): List<Borrowing> {
        return try {
            json.decodeFromString<List<BorrowingDto>>(cachedPayload).map { dto ->
                dto.toDomain()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode cached borrowings for userId=$userIdTag", e)
            emptyList()
        }
    }
}
