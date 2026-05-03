package com.example.kotlinclient.di

import com.example.kotlinclient.state_management.viewModel.EventTemplateViewModel
import com.example.kotlinclient.state_management.viewModel.EventViewModel
import com.example.kotlinclient.state_management.viewModel.InfoViewModel
import com.example.kotlinclient.state_management.viewModel.HomeViewModel
import com.example.kotlinclient.state_management.viewModel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        InfoViewModel(get(), get())
    }

    viewModel{
        HomeViewModel(get(),get())
    }

    viewModel{
        EventViewModel(get())
    }

    viewModel {
        EventTemplateViewModel(get())
    }

    viewModel {
        SettingsViewModel(get(), get())
    }
}