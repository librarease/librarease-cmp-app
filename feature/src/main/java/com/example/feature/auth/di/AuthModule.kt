package com.example.feature.auth.di

import com.example.feature.auth.data.remote.AuthApi
import com.example.feature.auth.data.remote.AuthApiImpl
import com.example.feature.auth.data.remote.AuthApiService
import com.example.feature.auth.data.remote.AuthServiceImpl
import com.example.feature.auth.data.repository.AuthRepositoryImpl
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.feature.auth.domain.usecase.IsUserLoggedInUseCase
import com.example.feature.auth.domain.usecase.SignInUseCase
import com.example.feature.auth.domain.usecase.SignOutUseCase
import com.example.feature.auth.domain.usecase.SignUpUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val authModule = module {
    
    single<AuthApi> { AuthApiImpl(get()) }
    
    single<AuthApiService> { AuthServiceImpl(get()) }
    
    single<AuthRepository> {
        AuthRepositoryImpl(
            authApiService = get(),
            context = androidContext()
        )
    }
    
    factory { SignUpUseCase(get()) }
    factory { SignInUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { IsUserLoggedInUseCase(get()) }
}
