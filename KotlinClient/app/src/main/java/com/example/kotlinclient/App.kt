package com.example.kotlinclient

import android.app.Application
import com.example.kotlinclient.di.databaseModule
import com.example.kotlinclient.di.preferencesModule
import com.example.kotlinclient.di.repositoryModule
import com.example.kotlinclient.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidContext(this@App)
            modules(
                databaseModule,
                repositoryModule,
                viewModelModule,
                preferencesModule
            )
        }
    }
}