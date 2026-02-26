package com.example.feature.home.di

import com.example.feature.home.data.remote.HomeApiService
import com.example.feature.home.data.repository.HomeRepositoryImpl
import com.example.feature.home.domain.repository.HomeRepository
import com.example.feature.home.domain.usecase.GetCachedBorrowingsUseCase
import com.example.feature.home.domain.usecase.GetCachedBookDetailUseCase
import com.example.feature.home.domain.usecase.GetCachedLibraryDetailUseCase
import com.example.feature.home.domain.usecase.GetBorrowingByIdUseCase
import com.example.feature.home.domain.usecase.GetBorrowingsUseCase
import com.example.feature.home.domain.usecase.GetBookDetailUseCase
import com.example.feature.home.domain.usecase.GetLibraryDetailUseCase
import com.example.feature.home.domain.usecase.GetSubscriptionByIdUseCase
import com.example.feature.home.domain.usecase.GetSubscriptionsUseCase
import com.example.feature.home.presentation.borrowingdetail.BorrowingDetailViewModel
import com.example.feature.home.presentation.home.HomeViewModel
import com.example.feature.home.presentation.subscriptiondetail.SubscriptionDetailViewModel
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single { HomeApiService(get(), get()) }

    single<HomeRepository> {
        HomeRepositoryImpl(
            homeApiService = get(),
            firebaseAuth = get(),
            context = androidContext(),
            json = get()
        )
    }

    factory { GetCachedBorrowingsUseCase(get()) }
    factory { GetCachedBookDetailUseCase(get()) }
    factory { GetCachedLibraryDetailUseCase(get()) }
    factory { GetBorrowingByIdUseCase(get()) }
    factory { GetBorrowingsUseCase(get()) }
    factory { GetBookDetailUseCase(get()) }
    factory { GetLibraryDetailUseCase(get()) }
    factory { GetSubscriptionByIdUseCase(get()) }
    factory { GetSubscriptionsUseCase(get()) }

    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { BorrowingDetailViewModel(get(), get(), get()) }
    viewModel { SubscriptionDetailViewModel(get(), get(), get()) }
}
