package com.example.kotlinclient.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.kotlinclient.state_management.viewModel.EventTemplateViewModel
import com.example.kotlinclient.state_management.viewModel.EventViewModel
import com.example.kotlinclient.state_management.viewModel.HomeViewModel
import com.example.kotlinclient.state_management.viewModel.InfoViewModel
import com.example.kotlinclient.state_management.viewModel.SettingsViewModel
import com.example.kotlinclient.state_management.viewModel.SharedAppViewModel
import org.koin.androidx.compose.koinViewModel

// Навигация приложения
@Composable
fun ApplicationNavHost(
    navController: NavHostController,
    startDestination: String,
    paddingValues: PaddingValues,
    sharedAppViewModel: SharedAppViewModel
) {

    val infoViewModel: InfoViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()
    val eventViewModel: EventViewModel = koinViewModel()
    val eventTemplateViewModel: EventTemplateViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()


    // Главный компонент навигации, определяет пути и экраны, которые будут вызваны по этому пути
    NavHost(navController, startDestination = startDestination) {

        homeScreen(
            homeViewModel,
            eventViewModel,
            infoViewModel,
            navController,
            paddingValues,
            sharedAppViewModel
        )

        infoScreen(
            infoViewModel,
            sharedAppViewModel,
            paddingValues
        )

        eventScreen(
            eventViewModel,
            eventTemplateViewModel,
            sharedAppViewModel,
            paddingValues
        )

        templateScreen(
            eventTemplateViewModel,
            paddingValues
        )

        settingsScreen(
            settingsViewModel,
            paddingValues
        )

    }

}





