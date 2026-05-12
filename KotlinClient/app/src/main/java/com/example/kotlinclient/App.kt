package com.example.kotlinclient

import android.app.Application
import com.example.kotlinclient.di.databaseModule
import com.example.kotlinclient.di.preferencesModule
import com.example.kotlinclient.di.repositoryModule
import com.example.kotlinclient.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App: Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidContext(this@App)
            modules(
                module { single { applicationScope } },
                databaseModule,
                preferencesModule,
                repositoryModule,
                viewModelModule,

            )
        }
    }
}