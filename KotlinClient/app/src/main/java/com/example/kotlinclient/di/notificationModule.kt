package com.example.kotlinclient.di

import com.example.kotlinclient.presentation.utility.EventAlarmScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val notificationModule = module {

    single { EventAlarmScheduler(androidContext()) }
}