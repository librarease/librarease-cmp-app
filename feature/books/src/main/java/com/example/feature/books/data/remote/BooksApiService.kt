package com.example.feature.books.data.remote

import com.example.core.model.book.BookDetailDto
import com.example.core.model.book.BookDetailResponse
import com.example.core.model.book.BookDto
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
        authToken: String? = null
    ): List<BookDto> {
        val response = httpClient.get("${ApiConfig.BASE_URL}books") {
            url {
                parameters.append("limit", limit.toString())
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
}
