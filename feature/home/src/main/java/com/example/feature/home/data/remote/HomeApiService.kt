package com.example.feature.home.data.remote

import com.example.core.network.ApiConfig
import com.example.feature.home.data.model.BorrowingDto
import com.example.feature.home.data.model.BorrowingsResponse
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
}
