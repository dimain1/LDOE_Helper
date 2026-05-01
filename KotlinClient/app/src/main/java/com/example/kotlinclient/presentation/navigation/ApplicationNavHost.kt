package com.example.kotlinclient.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
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
import com.example.kotlinclient.ui.theme.KotlinClientTheme

// Навигация приложения
@Composable
fun ApplicationNavHost(navController: NavHostController, startDestination: String, paddingValues: PaddingValues){

    // Главный компонент навигации, определяет пути и экраны, которые будут вызваны по этому пути
    NavHost(navController, startDestination = startDestination) {
        composable(route = Routes.HomePage.route){
            HomeScreen(paddingValues)
        }


        composable(route= Routes.InfoPage.route){
            InfoScreen(paddingValues)
        }


        composable(route= Routes.EventsPage.route){
            EventScreen(paddingValues)
        }


        composable(route= Routes.TemplatePage.route){
            TemplateScreen(paddingValues)
        }


        composable(route= Routes.SettingsPage.route){
            SettingsScreen(paddingValues)
        }

    }

}

// Функции навигации(Переходы)
fun navigateToHome(navController: NavController){
    navController.navigate(Routes.HomePage.route)
}

fun navigateToInfo(navController: NavController){
    navController.navigate(Routes.InfoPage.route)
}

fun navigateToEvent(navController: NavController){
    navController.navigate(Routes.EventsPage.route)
}

fun navigateToTemplate(navController: NavController){
    navController.navigate(Routes.TemplatePage.route)
}

fun navigateToSettings(navController: NavController){
    navController.navigate(Routes.SettingsPage.route)
}



