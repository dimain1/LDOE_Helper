package com.example.kotlinclient.di

import com.example.kotlinclient.state_management.repository.implementation.ContentTypeRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.EventRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.EventTemplateRepositoryImpl
import com.example.kotlinclient.state_management.repository.implementation.GameContentRepositoryImpl
import com.example.kotlinclient.state_management.repository.interfaces.ContentTypeRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<ContentTypeRepository> {
        ContentTypeRepositoryImpl(get())
    }

    single<GameContentRepository> {
        GameContentRepositoryImpl(get())
    }

    single<EventRepository>{
        EventRepositoryImpl(get())
    }

    single<EventTemplateRepository>{
        EventTemplateRepositoryImpl(get())
    }

}