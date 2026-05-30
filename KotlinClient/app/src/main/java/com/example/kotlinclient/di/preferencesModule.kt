package com.example.kotlinclient.di

import android.content.SharedPreferences
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesModule = module {

    single<SharedPreferences> {
        androidContext().getSharedPreferences("my_app_preferences", 0)
    }

    single<UserSessionProvider>{
        UserSession(get(), get(), get())
    }

}