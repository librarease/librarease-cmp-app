package com.example.feature.home.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.core.storage.authPrefsDataStore
import com.example.core.model.book.BookDetailDto
import com.example.feature.home.data.model.borrowing_model.BorrowingDto
import com.example.feature.home.data.model.library_model.LibraryDetailDto
import com.example.feature.home.data.model.subscription_model.SubscriptionDto
import com.example.feature.home.data.model.toDomain
import com.example.feature.home.data.model.toLibraryDetailDto
import com.example.feature.home.data.model.toDto
import com.example.feature.home.data.remote.HomeApiService
import com.example.core.model.book.BookDetail
import com.example.core.model.book.LibraryInfo
import com.example.feature.home.domain.model.Borrowing
import com.example.feature.home.domain.model.HomeResult
import com.example.feature.home.domain.model.Subscription
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

    override suspend fun getCachedBookDetail(bookId: String): BookDetail? {
        if (bookId.isBlank()) return null

        val preferences = context.authPrefsDataStore.data.first()
        val cacheKey = bookDetailCacheKey(bookId)
        val cachedPayload = preferences[cacheKey] ?: return null
        return decodeCachedBookDetail(cachedPayload, bookId)
    }

    override suspend fun getCachedLibraryDetail(libraryId: String): LibraryInfo? {
        if (libraryId.isBlank()) return null

        val preferences = context.authPrefsDataStore.data.first()
        val cacheKey = libraryDetailCacheKey(libraryId)
        val cachedPayload = preferences[cacheKey] ?: return null
        return decodeCachedLibraryDetail(cachedPayload, libraryId)
    }

    override suspend fun getCachedSubscriptions(userId: String?): List<Subscription> {
        val userIdCandidates = resolveUserIdCandidates(userId)

        val preferences = context.authPrefsDataStore.data.first()
        val latestCachedPayload = preferences[LATEST_SUBSCRIPTIONS_CACHE_KEY]
        if (!latestCachedPayload.isNullOrBlank()) {
            val latestDecoded = decodeCachedSubscriptions(latestCachedPayload, "latest")
            if (latestDecoded.isNotEmpty()) {
                return latestDecoded
            }
        }

        if (userIdCandidates.isEmpty()) return emptyList()

        userIdCandidates.forEach { candidateUserId ->
            val cacheKey = subscriptionsCacheKey(candidateUserId)
            val cachedPayload = preferences[cacheKey] ?: return@forEach
            val decoded = decodeCachedSubscriptions(cachedPayload, candidateUserId)
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

    override suspend fun getBookDetail(bookId: String): HomeResult<BookDetail> {
        if (bookId.isBlank()) {
            return HomeResult.Error("Book id is required")
        }

        val authToken = resolveAuthToken()
        return try {
            if (!authToken.isNullOrBlank()) {
                val withToken = homeApiService.getBookDetail(
                    bookId = bookId,
                    authToken = authToken
                ).toDomain()
                cacheBookDetail(bookId, withToken)
                HomeResult.Success(withToken)
            } else {
                val withoutToken = homeApiService.getBookDetail(bookId).toDomain()
                cacheBookDetail(bookId, withoutToken)
                HomeResult.Success(withoutToken)
            }
        } catch (primary: Exception) {
            if (authToken.isNullOrBlank()) {
                return HomeResult.Error(primary.message ?: "Failed to load book detail", primary)
            }

            try {
                val withoutToken = homeApiService.getBookDetail(bookId).toDomain()
                cacheBookDetail(bookId, withoutToken)
                HomeResult.Success(withoutToken)
            } catch (secondary: Exception) {
                HomeResult.Error(secondary.message ?: "Failed to load book detail", secondary)
            }
        }
    }

    override suspend fun getLibraryDetail(libraryId: String): HomeResult<LibraryInfo> {
        if (libraryId.isBlank()) {
            return HomeResult.Error("Library id is required")
        }

        val authToken = resolveAuthToken()
        return try {
            if (!authToken.isNullOrBlank()) {
                val withToken = homeApiService.getLibraryDetail(
                    libraryId = libraryId,
                    authToken = authToken
                ).toDomain()
                cacheLibraryDetail(libraryId, withToken)
                HomeResult.Success(withToken)
            } else {
                val withoutToken = homeApiService.getLibraryDetail(libraryId).toDomain()
                cacheLibraryDetail(libraryId, withoutToken)
                HomeResult.Success(withoutToken)
            }
        } catch (primary: Exception) {
            if (authToken.isNullOrBlank()) {
                return HomeResult.Error(primary.message ?: "Failed to load library detail", primary)
            }

            try {
                val withoutToken = homeApiService.getLibraryDetail(libraryId).toDomain()
                cacheLibraryDetail(libraryId, withoutToken)
                HomeResult.Success(withoutToken)
            } catch (secondary: Exception) {
                HomeResult.Error(secondary.message ?: "Failed to load library detail", secondary)
            }
        }
    }

    override suspend fun getSubscriptionById(
        subscriptionId: String,
        userId: String?
    ): HomeResult<Subscription> {
        if (subscriptionId.isBlank()) {
            return HomeResult.Error("Subscription id is required")
        }

        val cached = getCachedSubscriptions(userId).firstOrNull { subscription ->
            subscription.id == subscriptionId
        }
        if (cached != null) {
            return HomeResult.Success(cached)
        }

        return when (val subscriptionsResult = getSubscriptions(userId)) {
            is HomeResult.Success -> {
                val subscription = subscriptionsResult.data.firstOrNull { item ->
                    item.id == subscriptionId
                }
                if (subscription != null) {
                    HomeResult.Success(subscription)
                } else {
                    HomeResult.Error("Subscription not found")
                }
            }
            is HomeResult.Error -> HomeResult.Error(subscriptionsResult.message, subscriptionsResult.cause)
            is HomeResult.Loading -> HomeResult.Loading
        }
    }

    override suspend fun getSubscriptions(userId: String?): HomeResult<List<Subscription>> {
        val userIdCandidates = resolveUserIdCandidates(userId)
        if (userIdCandidates.isEmpty()) {
            return HomeResult.Error("No Firebase user id available. Please sign in first.")
        }

        val authToken = resolveAuthToken()
        var lastError: Exception? = null

        userIdCandidates.forEach { candidateUserId ->
            Log.d(TAG, "Trying subscriptions with userId=$candidateUserId")
            if (!authToken.isNullOrBlank()) {
                try {
                    val subscriptionDtosWithToken = homeApiService.getSubscriptions(
                        userId = candidateUserId,
                        authToken = authToken
                    )
                    cacheSubscriptions(candidateUserId, subscriptionDtosWithToken)
                    val subscriptionsWithToken = subscriptionDtosWithToken.map { dto ->
                        dto.toDomain()
                    }
                    return HomeResult.Success(subscriptionsWithToken)
                } catch (e: Exception) {
                    lastError = e
                    Log.w(TAG, "Subscriptions call failed with token for userId=$candidateUserId", e)
                }
            }

            try {
                val subscriptionDtosWithoutToken = homeApiService.getSubscriptions(userId = candidateUserId)
                cacheSubscriptions(candidateUserId, subscriptionDtosWithoutToken)
                val subscriptionsWithoutToken = subscriptionDtosWithoutToken.map { dto ->
                    dto.toDomain()
                }
                return HomeResult.Success(subscriptionsWithoutToken)
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "Subscriptions call failed without token for userId=$candidateUserId", e)
            }
        }

        Log.e(TAG, "Failed to load subscriptions for all user id candidates", lastError)
        return HomeResult.Error(
            message = lastError?.message ?: "Failed to load subscriptions",
            cause = lastError
        )
    }

    companion object {
        private const val TAG = "HomeRepository"
        private val USER_KEY = stringPreferencesKey("user_data")
        private val LATEST_BORROWINGS_CACHE_KEY = stringPreferencesKey("borrowings_cache_latest")
        private val LATEST_SUBSCRIPTIONS_CACHE_KEY = stringPreferencesKey("subscriptions_cache_latest")
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

    private suspend fun cacheBookDetail(bookId: String, bookDetail: BookDetail) {
        val cacheKey = bookDetailCacheKey(bookId)
        val encoded = json.encodeToString(bookDetail.toDto())
        context.authPrefsDataStore.edit { preferences ->
            preferences[cacheKey] = encoded
        }
    }

    private fun bookDetailCacheKey(bookId: String) = stringPreferencesKey("book_detail_cache_$bookId")

    private suspend fun cacheSubscriptions(
        userId: String,
        subscriptions: List<SubscriptionDto>
    ) {
        val cacheKey = subscriptionsCacheKey(userId)
        val encoded = json.encodeToString(subscriptions)
        context.authPrefsDataStore.edit { preferences ->
            preferences[cacheKey] = encoded
            preferences[LATEST_SUBSCRIPTIONS_CACHE_KEY] = encoded
        }
    }

    private fun subscriptionsCacheKey(userId: String) = stringPreferencesKey("subscriptions_cache_$userId")

    private suspend fun cacheLibraryDetail(libraryId: String, libraryInfo: LibraryInfo) {
        val cacheKey = libraryDetailCacheKey(libraryId)
        val encoded = json.encodeToString(libraryInfo.toLibraryDetailDto())
        context.authPrefsDataStore.edit { preferences ->
            preferences[cacheKey] = encoded
        }
    }

    private fun libraryDetailCacheKey(libraryId: String) = stringPreferencesKey("library_detail_cache_$libraryId")

    private fun decodeCachedBookDetail(
        cachedPayload: String,
        bookIdTag: String
    ): BookDetail? {
        return try {
            json.decodeFromString<BookDetailDto>(cachedPayload).toDomain()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode cached book detail for bookId=$bookIdTag", e)
            null
        }
    }

    private fun decodeCachedLibraryDetail(
        cachedPayload: String,
        libraryIdTag: String
    ): LibraryInfo? {
        return try {
            json.decodeFromString<LibraryDetailDto>(cachedPayload).toDomain()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode cached library detail for libraryId=$libraryIdTag", e)
            null
        }
    }

    private fun decodeCachedSubscriptions(
        cachedPayload: String,
        userIdTag: String
    ): List<Subscription> {
        return try {
            json.decodeFromString<List<SubscriptionDto>>(cachedPayload).map { dto ->
                dto.toDomain()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode cached subscriptions for userId=$userIdTag", e)
            emptyList()
        }
    }

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
