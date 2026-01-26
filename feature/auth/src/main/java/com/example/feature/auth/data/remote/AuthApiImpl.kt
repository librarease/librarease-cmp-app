package com.example.feature.auth.data.remote

import com.example.feature.auth.data.model.AuthResponse
import com.example.feature.auth.data.model.SignInRequest
import com.example.feature.auth.data.model.SignUpRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiImpl(
    private val httpClient: HttpClient
) : AuthApi {
    
    override suspend fun signUp(request: SignUpRequest): AuthResponse {
        return httpClient.post(AuthEndpoints.REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    override suspend fun signIn(request: SignInRequest): AuthResponse {
        return httpClient.post(AuthEndpoints.SIGNIN) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
