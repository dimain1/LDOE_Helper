package com.example.kotlinclient.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import androidx.navigation.NavHostController

import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kotlinclient.presentation.Info.InfoScreen
import com.example.kotlinclient.presentation.MyBottomAppBar
import com.example.kotlinclient.presentation.event.EventScreen
import com.example.kotlinclient.presentation.home.HomeScreen
import com.example.kotlinclient.presentation.settings.SettingsScreen
import com.example.kotlinclient.presentation.template.TemplateScreen
import com.example.kotlinclient.state_management.entity.ContentType
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.viewModel.EventTemplateViewModel
import com.example.kotlinclient.state_management.viewModel.EventViewModel
import com.example.kotlinclient.state_management.viewModel.HomeAction
import com.example.kotlinclient.state_management.viewModel.HomeViewModel
import com.example.kotlinclient.state_management.viewModel.InfoViewModel
import com.example.kotlinclient.state_management.viewModel.SettingsViewModel
import com.example.kotlinclient.ui.theme.KotlinClientTheme
import org.koin.androidx.compose.koinViewModel

// Навигация приложения
@Composable
fun ApplicationNavHost(navController: NavHostController, startDestination: String, paddingValues: PaddingValues){

    val infoViewModel: InfoViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()
    val eventViewModel: EventViewModel = koinViewModel()
    val eventTemplateViewModel: EventTemplateViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel ()



    // Главный компонент навигации, определяет пути и экраны, которые будут вызваны по этому пути
    NavHost(navController, startDestination = startDestination) {

        homeScreen(
            homeViewModel,
            eventViewModel,
            infoViewModel,
            navController,
            paddingValues
        )

        infoScreen(
            infoViewModel,
            paddingValues
        )

        eventScreen(
            eventViewModel,
            eventTemplateViewModel,
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





