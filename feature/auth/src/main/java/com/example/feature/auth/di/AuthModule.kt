package com.example.feature.auth.di

import com.example.feature.auth.data.remote.AuthApiService
import com.example.feature.auth.data.repository.AuthRepositoryImpl
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.feature.auth.domain.usecase.IsUserLoggedInUseCase
import com.example.feature.auth.domain.usecase.SignInUseCase
import com.example.feature.auth.domain.usecase.SignOutUseCase
import com.example.feature.auth.domain.usecase.SignUpUseCase
import com.example.feature.auth.presentation.signin.SignInViewModel
import com.example.feature.auth.presentation.signup.SignUpViewModel
import com.example.feature.auth.presentation.splash.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    
    single { AuthApiService(get()) }

    single { FirebaseAuth.getInstance() }
    
    single<AuthRepository> {
        AuthRepositoryImpl(
            authApiService = get(),
            firebaseAuth = get(),
            context = androidContext()
        )
    }
    
    factory { SignUpUseCase(get()) }
    factory { SignInUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { IsUserLoggedInUseCase(get()) }
    
    viewModel { SplashViewModel(get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
}
