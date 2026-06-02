package com.example.feature.books.data.remote

import com.example.core.model.book.BookDetailDto
import com.example.core.model.book.BookDetailResponse
import com.example.core.model.book.BookDto
import com.example.core.model.review.ReviewDto
import com.example.core.model.review.ReviewsResponse
import com.example.core.network.ApiConfig
import com.example.feature.books.data.model.BooksResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class BooksApiService(
    private val httpClient: HttpClient,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {

    suspend fun getBooks(
        limit: Int,
        skip: Int = 0,
        page: Int? = null,
        authToken: String? = null
    ): List<BookDto> {
        val response = httpClient.get("${ApiConfig.BASE_URL}books") {
            url {
                parameters.append("limit", limit.toString())
                if (page != null) {
                    parameters.append("page", page.toString())
                } else if (skip > 0) {
                    parameters.append("skip", skip.toString())
                }
            }
            if (!authToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
        }
        val payload = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Books request failed: ${response.status.value} ${response.status.description}")
        }
        return decodeBooks(payload)
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

    suspend fun getReviews(
        bookId: String,
        libraryId: String?,
        borrowingId: String?,
        skip: Int,
        limit: Int,
        authToken: String? = null
    ): List<ReviewDto> {
        val response = httpClient.get("${ApiConfig.BASE_URL}reviews") {
            url {
                parameters.append("book_id", bookId)
                if (!libraryId.isNullOrBlank()) parameters.append("library_id", libraryId)
                if (!borrowingId.isNullOrBlank()) parameters.append("borrowing_id", borrowingId)
                parameters.append("skip", skip.toString())
                parameters.append("limit", limit.toString())
            }
            if (!authToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
        }
        val payload = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Reviews request failed: ${response.status.value} ${response.status.description}")
        }
        return decodeReviews(payload)
    }

    private fun decodeBooks(payload: String): List<BookDto> {
        if (payload.isBlank()) {
            return emptyList()
        }

        return try {
            json.decodeFromString<BooksResponse>(payload).books
        } catch (primary: Exception) {
            try {
                json.decodeFromString<List<BookDto>>(payload)
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

    private fun decodeReviews(payload: String): List<ReviewDto> {
        if (payload.isBlank()) {
            return emptyList()
        }

        return try {
            json.decodeFromString<ReviewsResponse>(payload).let { response ->
                response.data
                    .ifEmpty { response.reviews }
                    .ifEmpty { response.items }
                    .ifEmpty { response.results }
            }
        } catch (primary: Exception) {
            try {
                json.decodeFromString<List<ReviewDto>>(payload)
            } catch (secondary: Exception) {
                throw primary
            }
        }
    }
}
