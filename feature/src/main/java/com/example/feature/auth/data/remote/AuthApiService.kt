package com.example.feature.auth.data.remote

import com.example.feature.auth.data.model.AuthResponse
import com.example.feature.auth.data.model.SignInRequest
import com.example.feature.auth.data.model.SignUpRequest

interface AuthApiService {
    suspend fun signUp(request: SignUpRequest): AuthResponse
    suspend fun signIn(request: SignInRequest): AuthResponse
}
