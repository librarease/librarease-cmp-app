package com.example.feature.books.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.core.model.book.BookDetail
import com.example.core.model.book.BookDetailDto
import com.example.core.model.book.BookDto
import com.example.core.model.book.BookSummary
import com.example.core.model.review.Review
import com.example.core.model.review.toDomain
import com.example.core.storage.authPrefsDataStore
import com.example.feature.books.data.model.toDomain
import com.example.feature.books.data.model.toDto
import com.example.feature.books.data.remote.BooksApiService
import com.example.feature.books.domain.model.BooksResult
import com.example.feature.books.domain.repository.BooksRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class BooksRepositoryImpl(
    private val booksApiService: BooksApiService,
    private val firebaseAuth: FirebaseAuth,
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) : BooksRepository {

    override suspend fun getCachedBooks(): List<BookSummary> {
        val preferences = context.authPrefsDataStore.data.first()
        val cachedPayload = preferences[LATEST_BOOKS_CACHE_KEY] ?: return emptyList()
        return decodeCachedBooks(cachedPayload)
    }

    override suspend fun getCachedBookDetail(bookId: String): BookDetail? {
        if (bookId.isBlank()) return null
        val preferences = context.authPrefsDataStore.data.first()
        val cacheKey = bookDetailCacheKey(bookId)
        val cachedPayload = preferences[cacheKey] ?: return null
        return decodeCachedBookDetail(cachedPayload, bookId)
    }

    override suspend fun getBooks(limit: Int, skip: Int): BooksResult<List<BookSummary>> {
        val safeLimit = limit.coerceAtLeast(1)
        val authToken = resolveAuthToken()

        if (!authToken.isNullOrBlank()) {
            try {
                val dtos = fetchBooksPage(safeLimit, skip, authToken)
                cacheBooks(dtos, skip)
                return BooksResult.Success(dtos.map { dto -> dto.toDomain() })
            } catch (e: Exception) {
                Log.w(TAG, "Books call failed with token", e)
            }
        }

        return try {
            val dtos = fetchBooksPage(safeLimit, skip, null)
            cacheBooks(dtos, skip)
            BooksResult.Success(dtos.map { dto -> dto.toDomain() })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load books", e)
            BooksResult.Error(e.message ?: "Failed to load books", e)
        }
    }

    override suspend fun getBookDetail(bookId: String): BooksResult<BookDetail> {
        if (bookId.isBlank()) {
            return BooksResult.Error("Book id is required")
        }

        return try {
            val book = booksApiService.getBookDetail(bookId).toDomain()
            cacheBookDetail(bookId, book)
            BooksResult.Success(book)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load book detail", e)
            BooksResult.Error(e.message ?: "Failed to load book detail", e)
        }
    }

    override suspend fun getBookReviews(
        bookId: String,
        libraryId: String?,
        borrowingId: String?,
        skip: Int,
        limit: Int
    ): BooksResult<List<Review>> {
        if (bookId.isBlank()) {
            return BooksResult.Error("Book id is required")
        }

        return try {
            val authToken = resolveAuthToken()
            val reviews = booksApiService.getReviews(
                bookId = bookId,
                libraryId = libraryId,
                borrowingId = borrowingId,
                skip = skip.coerceAtLeast(0),
                limit = limit.coerceAtLeast(1),
                authToken = authToken
            )
                .map { dto -> dto.toDomain() }
                .distinctBy { review -> review.stableKey() }
                .sortedByDescending { review -> review.createdAt.toEpochMillisOrMin() }

            BooksResult.Success(reviews)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load reviews", e)
            BooksResult.Error(mapReviewsError(e), e)
        }
    }

    private suspend fun cacheBooks(books: List<BookDto>, skip: Int) {
        val merged = if (skip <= 0) {
            books
        } else {
            val existing = readCachedBookDtos()
            (existing + books).distinctBy { it.id }
        }
        val encoded = json.encodeToString(merged)
        context.authPrefsDataStore.edit { preferences ->
            preferences[LATEST_BOOKS_CACHE_KEY] = encoded
        }
    }

    private fun decodeCachedBooks(payload: String): List<BookSummary> {
        return try {
            json.decodeFromString<List<BookDto>>(payload).map { dto -> dto.toDomain() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode cached books", e)
            emptyList()
        }
    }

    private suspend fun cacheBookDetail(bookId: String, bookDetail: BookDetail) {
        val cacheKey = bookDetailCacheKey(bookId)
        val encoded = json.encodeToString(bookDetail.toDto())
        context.authPrefsDataStore.edit { preferences ->
            preferences[cacheKey] = encoded
        }
    }

    private fun bookDetailCacheKey(bookId: String) = stringPreferencesKey("book_detail_cache_$bookId")

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

    private suspend fun readCachedBookDtos(): List<BookDto> {
        val preferences = context.authPrefsDataStore.data.first()
        val cachedPayload = preferences[LATEST_BOOKS_CACHE_KEY] ?: return emptyList()
        return try {
            json.decodeFromString<List<BookDto>>(cachedPayload)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read cached book dtos", e)
            emptyList()
        }
    }

    private suspend fun fetchBooksPage(
        limit: Int,
        skip: Int,
        authToken: String?
    ): List<BookDto> {
        val initial = booksApiService.getBooks(limit, skip, null, authToken)
        if (skip <= 0 || initial.isEmpty()) return initial

        val existing = readCachedBookDtos()
        val existingIds = existing.asSequence().map { it.id }.toSet()
        val hasNew = initial.any { dto -> dto.id !in existingIds }
        if (hasNew) return initial

        val page = (skip / limit) + 1
        return try {
            val fallback = booksApiService.getBooks(limit, skip = 0, page = page, authToken = authToken)
            val fallbackHasNew = fallback.any { dto -> dto.id !in existingIds }
            if (fallbackHasNew) fallback else initial
        } catch (e: Exception) {
            Log.w(TAG, "Books pagination fallback failed", e)
            initial
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

    private fun mapReviewsError(error: Exception): String {
        val message = error.message.orEmpty()
        return when {
            "401" in message -> "You are not authorized. Please sign in again."
            "403" in message -> "You do not have permission to view these reviews."
            "404" in message -> "Reviews were not found for this book."
            "500" in message || "501" in message || "502" in message || "503" in message || "504" in message ->
                "Server error. Please try again later."
            message.contains("timeout", ignoreCase = true) ->
                "Request timed out. Please try again."
            message.contains("network", ignoreCase = true) ||
                message.contains("internet", ignoreCase = true) ||
                message.contains("unable to resolve", ignoreCase = true) ->
                "No internet connection. Please check your network."
            else -> message.ifBlank { "Failed to load reviews" }
        }
    }

    private fun Review.stableKey(): String {
        return id.ifBlank {
            listOf(comment.orEmpty(), createdAt.orEmpty(), reviewerName.orEmpty())
                .joinToString("|")
        }
    }

    private fun String?.toEpochMillisOrMin(): Long {
        if (this.isNullOrBlank()) return Long.MIN_VALUE

        val utcParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dateOnlyParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return runCatching { utcParser.parse(this)?.time }.getOrNull()
            ?: runCatching { dateOnlyParser.parse(this)?.time }.getOrNull()
            ?: Long.MIN_VALUE
    }

    companion object {
        private const val TAG = "BooksRepository"
        private val LATEST_BOOKS_CACHE_KEY = stringPreferencesKey("books_cache_latest")
    }
}
