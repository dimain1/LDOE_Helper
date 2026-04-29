package com.example.kotlinclient.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController

import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kotlinclient.presentation.home.HomeScreen
import com.example.kotlinclient.ui.theme.KotlinClientTheme

@Composable
fun ApplicationNavHost(navController: NavHostController, startDestination: String){

    NavHost(navController, startDestination = startDestination) {
        composable(route = Routes.HomePage.route){
            HomeScreen()}
    }

}




