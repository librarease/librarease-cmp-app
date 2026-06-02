package com.example.feature.settings.di

import com.example.feature.settings.data.SettingsPreferencesRepository
import com.example.feature.settings.presentation.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single { SettingsPreferencesRepository(androidContext()) }

    viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
}
