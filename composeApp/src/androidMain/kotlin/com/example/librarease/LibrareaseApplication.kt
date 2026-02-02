package com.example.librarease

import android.app.Application
import com.example.core.di.coreModule
import com.example.feature.auth.di.authModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class LibrareaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@LibrareaseApplication)
            modules(
                coreModule,
                authModule
            )
        }
    }
}
