package com.example.feature.libraries.data.remote

import com.example.core.network.ApiConfig
import com.example.feature.libraries.data.model.LibrariesResponse
import com.example.feature.libraries.data.model.LibraryItemDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class LibrariesApiService(
    private val httpClient: HttpClient,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {

    suspend fun getLibraries(
        limit: Int? = null,
        authToken: String? = null
    ): List<LibraryItemDto> {
        val response = httpClient.get("${ApiConfig.BASE_URL}libraries") {
            url {
                if (limit != null && limit > 0) {
                    parameters.append("limit", limit.toString())
                }
            }
            if (!authToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
        }
        val payload = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Libraries request failed: ${response.status.value} ${response.status.description}")
        }
        return decodeLibraries(payload)
    }

    private fun decodeLibraries(payload: String): List<LibraryItemDto> {
        if (payload.isBlank()) {
            return emptyList()
        }

        return try {
            json.decodeFromString<LibrariesResponse>(payload).libraries
        } catch (primary: Exception) {
            try {
                json.decodeFromString<List<LibraryItemDto>>(payload)
            } catch (secondary: Exception) {
                throw primary
            }
        }
    }
}
