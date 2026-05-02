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
import com.example.kotlinclient.state_management.viewModel.HomeViewModel
import com.example.kotlinclient.state_management.viewModel.InfoViewModel
import com.example.kotlinclient.ui.theme.KotlinClientTheme
import org.koin.androidx.compose.koinViewModel

// Навигация приложения
@Composable
fun ApplicationNavHost(navController: NavHostController, startDestination: String, paddingValues: PaddingValues){

    val infoViewModel: InfoViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()
    val eventViewModel: EventViewModel = koinViewModel()
    val eventTemplateViewModel: EventTemplateViewModel = koinViewModel()

    // Главный компонент навигации, определяет пути и экраны, которые будут вызваны по этому пути
    NavHost(navController, startDestination = startDestination) {
        composable(route = Routes.HomePage.route){

            val upcomingEvents = homeViewModel.upcomingEvents.collectAsState()
            val pinnedEntity = homeViewModel.pinnedEntity.collectAsState()

            HomeScreen(
                upcomingEvents = upcomingEvents.value,
                onDeleteClick = { id -> eventViewModel.deleteEventById(id) },
                pinnedEntity = pinnedEntity.value,
                onPinClick = { id, pinStatus -> infoViewModel.updateContentPin(id, pinStatus) },
                onNewEventClick = { navigateToEvent(navController) },
                onTemplateClick = { navigateToTemplate(navController) },
                onMyEventClick = { navigateToEvent(navController) },
                onDatabaseClick = { navigateToInfo(navController) },
                paddingValues = paddingValues
            )
        }


        composable(route= Routes.InfoPage.route){

            val types = infoViewModel.types.collectAsState()
            val selected_type = infoViewModel.selected_type.collectAsState()
            Log.e("types","types: ${types.value.size}")

            val gameContent = infoViewModel.gameContent.collectAsState()

            InfoScreen(
                paddingValues,
                types.value,
                selected_type.value,
                { id -> infoViewModel.selectType(id) },
                gameContent.value,
                {query -> infoViewModel.changeSearchQuery(query)},
                {infoViewModel.clearQuery()},
                {id, pinStatus ->infoViewModel.updateContentPin(id, pinStatus)}


            )
        }


        composable(route= Routes.EventsPage.route){

            val events = eventViewModel.Events.collectAsState()

            EventScreen(
                events= events.value,
                onEditClick = { id -> eventViewModel.deleteEventById(id) },
                onDeleteClick = { id -> eventViewModel.deleteEventById(id) },
                onCreateClick = {},
                paddingValues = paddingValues)
        }


        composable(route= Routes.TemplatePage.route){

            val templates = eventTemplateViewModel.templates.collectAsState()

            TemplateScreen(
                templates= templates.value,
                onEditClick = { id -> eventTemplateViewModel.deleteTemplate(id) },
                onDeleteClick = {id -> eventTemplateViewModel.deleteTemplate(id) },
                onCreateClick = {},
                paddingValues= paddingValues)
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



