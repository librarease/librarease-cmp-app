package com.example.feature.books.di

import com.example.feature.books.data.remote.BooksApiService
import com.example.feature.books.data.repository.BooksRepositoryImpl
import com.example.feature.books.domain.repository.BooksRepository
import com.example.feature.books.domain.usecase.GetBookDetailUseCase
import com.example.feature.books.domain.usecase.GetBooksUseCase
import com.example.feature.books.domain.usecase.GetCachedBooksUseCase
import com.example.feature.books.presentation.books.BooksViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val booksModule = module {
    single { BooksApiService(get()) }

    single<BooksRepository> {
        BooksRepositoryImpl(
            booksApiService = get(),
            firebaseAuth = get(),
            context = androidContext()
        )
    }

    factory { GetBooksUseCase(get()) }
    factory { GetBookDetailUseCase(get()) }
    factory { GetCachedBooksUseCase(get()) }

    viewModel { BooksViewModel(get(), get()) }
}
