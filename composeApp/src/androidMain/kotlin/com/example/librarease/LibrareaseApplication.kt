package com.example.librarease

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.core.di.coreModule
import com.example.feature.auth.di.authModule
import com.example.feature.books.di.booksModule
import com.example.feature.home.di.homeModule
import com.example.librarease.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class LibrareaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@LibrareaseApplication)
            modules(
                coreModule,
                authModule,
                homeModule,
                booksModule,
                appModule
            )
        }
    }
}
