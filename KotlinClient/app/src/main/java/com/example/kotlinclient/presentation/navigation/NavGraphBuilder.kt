package com.example.kotlinclient.presentation.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.kotlinclient.presentation.Info.InfoScreen
import com.example.kotlinclient.presentation.event.EventScreen
import com.example.kotlinclient.presentation.home.HomeScreen
import com.example.kotlinclient.presentation.settings.SettingsScreen
import com.example.kotlinclient.presentation.template.TemplateScreen
import com.example.kotlinclient.state_management.viewModel.EventAction
import com.example.kotlinclient.state_management.viewModel.EventCreateAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateViewModel
import com.example.kotlinclient.state_management.viewModel.EventViewModel
import com.example.kotlinclient.state_management.viewModel.HomeAction
import com.example.kotlinclient.state_management.viewModel.HomeViewModel
import com.example.kotlinclient.state_management.viewModel.InfoAction
import com.example.kotlinclient.state_management.viewModel.InfoViewModel
import com.example.kotlinclient.state_management.viewModel.SettingsAction
import com.example.kotlinclient.state_management.viewModel.SettingsViewModel
import com.example.kotlinclient.state_management.viewModel.ValidationEvent

fun NavGraphBuilder.homeScreen(
    homeViewModel: HomeViewModel,
    eventViewModel: EventViewModel,
    infoViewModel: InfoViewModel,
    navController: NavController,
    paddingValues: PaddingValues
)
{
    composable(route = Routes.HomePage.route){
        val uiState = homeViewModel.uiState.collectAsState()

        HomeScreen(
            uiState = uiState.value,
            onAction = { action ->
                when (action) {
                    is HomeAction.DeleteEvent -> eventViewModel.deleteEvent(action.id)
                    is HomeAction.TogglePin ->
                        infoViewModel.updateContentPin(
                        action.id,
                        action.pinStatus
                    )

                    is HomeAction.ToEvent -> navigateToEvent(navController)
                    is HomeAction.ToInfo -> navigateToInfo(navController)
                    is HomeAction.ToTemplate -> navigateToTemplate(navController)
                    is HomeAction.ToSettings -> navigateToSettings(navController)
                }
            },
            paddingValues = paddingValues
        )
    }
}

fun NavGraphBuilder.infoScreen(
    infoViewModel: InfoViewModel,
    paddingValues: PaddingValues
)
{
    composable(route= Routes.InfoPage.route){

        val uiState = infoViewModel.uiState.collectAsState()

        InfoScreen(
            uiState= uiState.value,
            onAction = { action ->
                when(action){
                    is InfoAction.SelectType -> {infoViewModel.selectType(action.id)}
                    is InfoAction.ChangeSearchQuery -> {infoViewModel.changeSearchQuery(action.query)}
                    is InfoAction.ClearQuery -> {infoViewModel.clearQuery()}
                    is InfoAction.UpdateContentPin -> {infoViewModel.updateContentPin(action.id, action.pinStatus)}
                }
            },
            paddingValues
        )
    }
}

fun NavGraphBuilder.eventScreen(
    eventViewModel: EventViewModel,
    eventTemplateViewModel: EventTemplateViewModel,
    paddingValues: PaddingValues
)
{
    composable(route= Routes.EventsPage.route){

        val context = LocalContext.current

        LaunchedEffect(Unit) {
            eventViewModel.validationEvents.collect { event ->
                when (event) {
                    is ValidationEvent.EmptyName -> Toast.makeText(context, "Имя пустое", Toast.LENGTH_SHORT).show()
                    is ValidationEvent.InvalidTime -> Toast.makeText(context, "Ошибка времени", Toast.LENGTH_SHORT).show()
                    is ValidationEvent.SuccessCreate -> Toast.makeText(context, "Успешно создано!", Toast.LENGTH_SHORT).show()
                    is ValidationEvent.SuccessUpdate -> Toast.makeText(context, "Успешно изменено!", Toast.LENGTH_SHORT ).show()
                }
            }
        }

        val eventUiState = eventViewModel.uiState.collectAsState()
        val templateUiState = eventTemplateViewModel.uiState.collectAsState()

        val createUiState= eventViewModel.createUiState.collectAsState()


        EventScreen(
            uiState= eventUiState.value,
//            onEditClick = { id -> eventViewModel.deleteEventById(id) },
//            onDeleteClick = { id -> eventViewModel.deleteEventById(id) },
//            onCreateClick = {},
            templates = templateUiState.value.templates,
            onAction = { action ->
                when(action) {
                    is EventAction.DeleteEvent -> eventViewModel.deleteEvent(action.id)
                }
            },
            createUiState= createUiState.value,
            onCreateAction= {action ->
                when(action) {
                    is EventCreateAction.SelectTemplate -> eventViewModel.selectTemplate(action.template)
                    is EventCreateAction.ValidateAndSave -> eventViewModel.saveEvent()
                    is EventCreateAction.UpdateEndTime -> eventViewModel.updateEndTime()
                    is EventCreateAction.LoadUiState -> eventViewModel.loadUiState(action.id)
                    is EventCreateAction.ClearUiState -> eventViewModel.clearUiState()
                }
            },
            paddingValues = paddingValues)
    }
}

fun NavGraphBuilder.templateScreen(
    eventTemplateViewModel: EventTemplateViewModel,
    paddingValues: PaddingValues
)
{
    composable(route= Routes.TemplatePage.route){

        val uiState = eventTemplateViewModel.uiState.collectAsState()

        TemplateScreen(templates = uiState.value.templates, onAction = { action ->
                when (action) {
                    is EventTemplateAction.DeleteTemplate -> eventTemplateViewModel.deleteTemplate(
                        action.id
                    )
                }
            }, paddingValues = paddingValues)
    }
}

fun NavGraphBuilder.settingsScreen(
    settingsViewModel: SettingsViewModel,
    paddingValues: PaddingValues
)
{
    composable(route= Routes.SettingsPage.route){

        val uiState = settingsViewModel.uiState.collectAsState()

        SettingsScreen(
            uiState = uiState.value,
            onAction = { action ->
                when(action){
                    is SettingsAction.SwitchPreference -> settingsViewModel.switchBooleanPreferences(action.key)
                    is SettingsAction.SetUserId -> settingsViewModel.setUserId(action.id)
                    is SettingsAction.EditUserInfo -> settingsViewModel.updateUserInfo(action.login, action.email)
                    is SettingsAction.ExitProfile -> settingsViewModel.exitProfile()
                }
            },
//            onSwitchClick = { key -> settingsViewModel.switchBooleanPreferences(key)},
//            onAuthClick = { id -> settingsViewModel.setUserId(id) },
//            onApproveClick = {login, email -> settingsViewModel.updateUserInfo(login, email)},
//            onExitClick = {settingsViewModel.setUserId(-1)},
            paddingValues= paddingValues)
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