package com.example.feature.auth.data.remote

import com.example.feature.auth.data.model.AuthResponse
import com.example.feature.auth.data.model.SignInRequest
import com.example.feature.auth.data.model.SignUpRequest

class AuthServiceImpl(
    private val api: AuthApi
) : AuthApiService {
    
    override suspend fun signUp(request: SignUpRequest): AuthResponse {
        return api.signUp(request)
    }

    override suspend fun signIn(request: SignInRequest): AuthResponse {
        return api.signIn(request)
    }
}