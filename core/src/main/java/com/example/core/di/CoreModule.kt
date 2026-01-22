package com.example.core.di

import com.example.core.network.HttpClientProvider
import org.koin.dsl.module

val coreModule = module {
    single { HttpClientProvider.provideHttpClient() }
}
