package com.example.feature.home.data.remote

import com.example.core.network.ApiConfig
import com.example.core.model.book.BookDetailDto
import com.example.core.model.book.BookDetailResponse
import com.example.core.model.review.CreateReviewRequest
import com.example.core.model.review.ReviewDto
import com.example.core.model.review.ReviewsResponse
import com.example.feature.home.data.model.borrowing_model.BorrowingDto
import com.example.feature.home.data.model.borrowing_model.BorrowingsResponse
import com.example.feature.home.data.model.library_model.LibraryDetailDto
import com.example.feature.home.data.model.library_model.LibraryDetailResponse
import com.example.feature.home.data.model.subscription_model.SubscriptionDto
import com.example.feature.home.data.model.subscription_model.SubscriptionListResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class HomeApiService(
    private val httpClient: HttpClient,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {

    suspend fun getBorrowings(
        userId: String,
        authToken: String? = null
    ): List<BorrowingDto> {
        val response = httpClient.get("${ApiConfig.BASE_URL}borrowings") {
            url {
                parameters.append("userId", userId)
            }
            if (!authToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
        }
        val payload = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Borrowings request failed: ${response.status.value} ${response.status.description}")
        }
        return decodeBorrowings(payload)
    }

    suspend fun getBookDetail(
        bookId: String,
        authToken: String? = null
    ): BookDetailDto {
        val response = httpClient.get("${ApiConfig.BASE_URL}books/$bookId") {
            if (!authToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
        }
        val payload = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Book detail request failed: ${response.status.value} ${response.status.description}")
        }
        return decodeBookDetail(payload)
    }

    suspend fun getSubscriptions(
        userId: String,
        authToken: String? = null
    ): List<SubscriptionDto> {
        val response = httpClient.get("${ApiConfig.BASE_URL}subscriptions") {
            url {
                parameters.append("userId", userId)
            }
            if (!authToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
        }
        val payload = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Subscriptions request failed: ${response.status.value} ${response.status.description}")
        }
        return decodeSubscriptions(payload)
    }

    suspend fun getLibraryDetail(
        libraryId: String,
        authToken: String? = null
    ): LibraryDetailDto {
        val response = httpClient.get("${ApiConfig.BASE_URL}libraries/$libraryId") {
            if (!authToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
        }
        val payload = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Library detail request failed: ${response.status.value} ${response.status.description}")
        }
        return decodeLibraryDetail(payload)
    }

    suspend fun submitReview(
        request: CreateReviewRequest,
        authToken: String? = null
    ): ReviewDto {
        val url = "${ApiConfig.BASE_URL}reviews"
        android.util.Log.d("HomeApiService", "Submitting review to: $url")
        android.util.Log.d("HomeApiService", "Request: borrowingId=${request.borrowingId}, rating=${request.rating}, comment=${request.comment}")
        android.util.Log.d("HomeApiService", "Auth token present: ${!authToken.isNullOrBlank()}")
        
        val response = try {
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
                if (!authToken.isNullOrBlank()) {
                    header(HttpHeaders.Authorization, "Bearer $authToken")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeApiService", "Network error during review submission", e)
            throw IllegalStateException("Network error: ${e.message}", e)
        }
        
        val payload = response.bodyAsText()
        android.util.Log.d("HomeApiService", "Response status: ${response.status.value} ${response.status.description}")
        android.util.Log.d("HomeApiService", "Response body: $payload")
        
        if (!response.status.isSuccess()) {
            android.util.Log.e("HomeApiService", "Submit review failed: ${response.status.value} ${response.status.description}")
            android.util.Log.e("HomeApiService", "Response body: $payload")
            android.util.Log.e("HomeApiService", "Request: borrowingId=${request.borrowingId}, rating=${request.rating}")
            throw IllegalStateException("Submit review request failed: ${response.status.value} ${response.status.description}")
        }
        
        return try {
            json.decodeFromString<ReviewDto>(payload)
        } catch (e: Exception) {
            android.util.Log.w("HomeApiService", "Could not parse review response, creating minimal ReviewDto", e)
            ReviewDto(
                id = request.borrowingId,
                borrowingId = request.borrowingId,
                rating = request.rating.toDouble(),
                comment = request.comment
            )
        }
    }

    suspend fun getReviewsForBook(
        bookId: String,
        authToken: String? = null
    ): List<ReviewDto> {
        val response = httpClient.get("${ApiConfig.BASE_URL}reviews") {
            url {
                parameters.append("book_id", bookId)
            }
            if (!authToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
        }
        val payload = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Get reviews request failed: ${response.status.value} ${response.status.description}")
        }
        return decodeReviews(payload)
    }

    private fun decodeReviews(payload: String): List<ReviewDto> {
        if (payload.isBlank()) {
            return emptyList()
        }

        return try {
            val response = json.decodeFromString<ReviewsResponse>(payload)
            response.data.ifEmpty { 
                response.reviews.ifEmpty { 
                    response.items.ifEmpty { 
                        response.results 
                    } 
                } 
            }
        } catch (primary: Exception) {
            try {
                json.decodeFromString<List<ReviewDto>>(payload)
            } catch (secondary: Exception) {
                android.util.Log.w("HomeApiService", "Could not parse reviews response", primary)
                emptyList()
            }
        }
    }

    private fun decodeBorrowings(payload: String): List<BorrowingDto> {
        if (payload.isBlank()) {
            return emptyList()
        }

        return try {
            json.decodeFromString<BorrowingsResponse>(payload).borrowings
        } catch (primary: Exception) {
            try {
                json.decodeFromString<List<BorrowingDto>>(payload)
            } catch (secondary: Exception) {
                throw primary
            }
        }
    }

    private fun decodeBookDetail(payload: String): BookDetailDto {
        if (payload.isBlank()) {
            throw IllegalStateException("Book detail payload was empty")
        }

        return try {
            json.decodeFromString<BookDetailResponse>(payload).data
                ?: throw IllegalStateException("Book detail response missing data")
        } catch (primary: Exception) {
            try {
                json.decodeFromString<BookDetailDto>(payload)
            } catch (secondary: Exception) {
                throw primary
            }
        }
    }

    private fun decodeSubscriptions(payload: String): List<SubscriptionDto> {
        if (payload.isBlank()) {
            return emptyList()
        }

        return try {
            json.decodeFromString<SubscriptionListResponse>(payload).subscriptions.orEmpty()
        } catch (primary: Exception) {
            try {
                json.decodeFromString<List<SubscriptionDto>>(payload)
            } catch (secondary: Exception) {
                throw primary
            }
        }
    }

    private fun decodeLibraryDetail(payload: String): LibraryDetailDto {
        if (payload.isBlank()) {
            throw IllegalStateException("Library detail payload was empty")
        }

        return try {
            json.decodeFromString<LibraryDetailResponse>(payload).data
                ?: throw IllegalStateException("Library detail response missing data")
        } catch (primary: Exception) {
            try {
                json.decodeFromString<LibraryDetailDto>(payload)
            } catch (secondary: Exception) {
                throw primary
            }
        }
    }
}
