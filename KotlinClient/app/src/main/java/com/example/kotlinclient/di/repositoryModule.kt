package com.example.kotlinclient.di

import com.example.kotlinclient.presentation.utility.ImageStorageManager
import com.example.kotlinclient.state_management.repository.implementation.ContentTypeRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.EventRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.EventTemplateRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.GameContentRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.SharedPreferencesRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.UserRepositoryImpl
import com.example.kotlinclient.state_management.repository.interfaces.ContentTypeRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single<ContentTypeRepository> {
        ContentTypeRepositoryImpl(get())
    }

    single<GameContentRepository> {
        GameContentRepositoryImpl(get(), get())
    }

    single<EventRepository>{
        EventRepositoryImpl(get(), get())
    }

    single<EventTemplateRepository>{
        EventTemplateRepositoryImpl(get(), get())
    }

    single<UserRepository>{
        UserRepositoryImpl(get())
    }

    single<SharedPreferencesRepository>{
        SharedPreferencesRepositoryImpl(get())
    }

    single<ImageStorageManager>{
        ImageStorageManager(androidContext())
    }

}