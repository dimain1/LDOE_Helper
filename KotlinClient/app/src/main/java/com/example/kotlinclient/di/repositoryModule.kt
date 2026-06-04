package com.example.kotlinclient.di

import com.example.kotlinclient.api_client.ApiService
import com.example.kotlinclient.api_client.TokenStorage
import com.example.kotlinclient.state_management.utility.EventAlarmScheduler
import com.example.kotlinclient.state_management.utility.ImageStorageManager
import com.example.kotlinclient.state_management.repository.implementation.AuthRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.ContentTypeRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.EventRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.EventTemplateRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.GameContentRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.SharedPreferencesRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.UserRepositoryImpl
import com.example.kotlinclient.state_management.repository.interfaces.AuthRepository
import com.example.kotlinclient.state_management.repository.interfaces.ContentTypeRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single<SharedPreferencesRepository> {
        SharedPreferencesRepositoryImpl(get())
    }

    single<UserRepository> {
        UserRepositoryImpl(get())
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            api = get<ApiService>(),
            tokenStorage = get<TokenStorage>(),
            sharedPrefs = get<SharedPreferencesRepository>(),
            alarmScheduler = get<EventAlarmScheduler>(),
            database = get()
        )
    }

    single<ContentTypeRepository> {
        ContentTypeRepositoryImpl(get(), get<ApiService>())
    }

    single<GameContentRepository> {
        GameContentRepositoryImpl(get(), get(), get<ApiService>())
    }

    single<EventRepository> {
        EventRepositoryImpl(get(), get(), get<ApiService>())
    }

    single<EventTemplateRepository> {
        EventTemplateRepositoryImpl(get(), get(), get<ApiService>())
    }

    single<ImageStorageManager> {
        ImageStorageManager(androidContext())
    }
}
