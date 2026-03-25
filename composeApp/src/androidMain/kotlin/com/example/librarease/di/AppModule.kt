package com.example.librarease.di

import com.example.librarease.navigation.AppStartViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AppStartViewModel(get(), get(), get()) }
}
