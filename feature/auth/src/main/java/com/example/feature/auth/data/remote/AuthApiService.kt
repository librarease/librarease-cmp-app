package com.example.feature.auth.data.remote

import com.example.core.network.ApiConfig
import com.example.feature.auth.data.model.AuthResponse
import com.example.feature.auth.data.model.SignInRequest
import com.example.feature.auth.data.model.SignUpRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiService(private val httpClient: HttpClient) {
    
    suspend fun signUp(request: SignUpRequest): AuthResponse {
        return httpClient.post("${ApiConfig.BASE_URL}auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun signIn(request: SignInRequest): AuthResponse {
        return httpClient.post("${ApiConfig.BASE_URL}auth/signin") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
