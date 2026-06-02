package com.example.feature.libraries.di

import com.example.feature.libraries.data.remote.LibrariesApiService
import com.example.feature.libraries.data.repository.LibrariesRepositoryImpl
import com.example.feature.libraries.domain.repository.LibrariesRepository
import com.example.feature.libraries.domain.usecase.GetCachedLibrariesUseCase
import com.example.feature.libraries.domain.usecase.GetLibrariesUseCase
import com.example.feature.libraries.presentation.libraries.LibrariesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val librariesModule = module {
    single { LibrariesApiService(get()) }

    single<LibrariesRepository> {
        LibrariesRepositoryImpl(
            librariesApiService = get(),
            firebaseAuth = get(),
            context = androidContext()
        )
    }

    factory { GetLibrariesUseCase(get()) }
    factory { GetCachedLibrariesUseCase(get()) }

    viewModel { LibrariesViewModel(get(), get(), get()) }
}
