package com.example.feature.auth.data.remote

import com.example.core.network.ApiConfig
import com.example.feature.auth.data.model.AuthResponse
import com.example.feature.auth.data.model.SignInRequest
import com.example.feature.auth.data.model.SignUpRequest

interface AuthApi {
    suspend fun signUp(request: SignUpRequest): AuthResponse
    suspend fun signIn(request: SignInRequest): AuthResponse
}

object AuthEndpoints {
    const val REGISTER = "${ApiConfig.BASE_URL}auth/register"
    const val SIGNIN = "${ApiConfig.BASE_URL}auth/signin"
}