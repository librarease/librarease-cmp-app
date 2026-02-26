package com.example.feature.home.data.remote

import com.example.core.network.ApiConfig
import com.example.feature.home.data.model.bood_model.BookDetailDto
import com.example.feature.home.data.model.bood_model.BookDetailResponse
import com.example.feature.home.data.model.borrowing_model.BorrowingDto
import com.example.feature.home.data.model.borrowing_model.BorrowingsResponse
import com.example.feature.home.data.model.library_model.LibraryDetailDto
import com.example.feature.home.data.model.library_model.LibraryDetailResponse
import com.example.feature.home.data.model.subscription_model.SubscriptionDto
import com.example.feature.home.data.model.subscription_model.SubscriptionListResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
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
