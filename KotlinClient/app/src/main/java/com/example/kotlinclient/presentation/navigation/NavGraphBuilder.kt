package com.example.kotlinclient.presentation.navigation

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.kotlinclient.presentation.Info.InfoScreen
import com.example.kotlinclient.presentation.event.EventScreen
import com.example.kotlinclient.presentation.home.HomeScreen
import com.example.kotlinclient.presentation.settings.SettingsScreen
import com.example.kotlinclient.presentation.template.TemplateScreen
import com.example.kotlinclient.state_management.viewModel.DialogType
import com.example.kotlinclient.state_management.viewModel.EventAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateViewModel
import com.example.kotlinclient.state_management.viewModel.EventViewModel
import com.example.kotlinclient.state_management.viewModel.HomeAction
import com.example.kotlinclient.state_management.viewModel.HomeViewModel
import com.example.kotlinclient.state_management.viewModel.InfoAction
import com.example.kotlinclient.state_management.viewModel.InfoViewModel
import com.example.kotlinclient.state_management.viewModel.SettingsViewModel
import com.example.kotlinclient.state_management.viewModel.SharedAction
import com.example.kotlinclient.state_management.viewModel.SharedAppViewModel

fun NavGraphBuilder.homeScreen(
    homeViewModel: HomeViewModel,
    eventViewModel: EventViewModel,
    infoViewModel: InfoViewModel,
    navController: NavController,
    paddingValues: PaddingValues,
    sharedAppViewModel: SharedAppViewModel
) {
    composable(route = Routes.HomePage.route) {
        val uiState = homeViewModel.uiState.collectAsState()
        val context: Context = LocalContext.current

        HomeScreen(
            uiState = uiState.value,
            onAction = { action ->
                when (action) {
                    is HomeAction.LaunchOverlay -> homeViewModel.onAction(
                        HomeAction.LaunchOverlay(
                            context
                        )
                    )

                    is HomeAction.DeleteEvent -> eventViewModel.onAction(
                        EventAction.DeleteEvent(
                            action.id
                        )
                    )

                    is HomeAction.TogglePin ->
                        infoViewModel.onAction(
                            InfoAction.UpdateContentPin(
                                action.id,
                                action.pinStatus
                            )
                        )

                    is HomeAction.ToEvent -> navigateToEvent(navController)
                    is HomeAction.ToInfo -> navigateToInfo(navController)
                    is HomeAction.ToTemplate -> navigateToTemplate(navController)
                    is HomeAction.ToSettings -> navigateToSettings(navController)
                    is HomeAction.ShowEventDetails -> sharedAppViewModel.onAction(SharedAction.ChangeDialogVisibility(true,
                        DialogType.EventViewDetailsDialog(action.initialData)))
                }
            },
            paddingValues = paddingValues
        )
    }
}

fun NavGraphBuilder.infoScreen(
    infoViewModel: InfoViewModel,
    paddingValues: PaddingValues
) {
    composable(route = Routes.InfoPage.route) {

        val uiState = infoViewModel.uiState.collectAsState()

        InfoScreen(
            uiState = uiState.value,
            onAction = { action -> infoViewModel.onAction(action) },
            paddingValues
        )
    }
}

fun NavGraphBuilder.eventScreen(
    eventViewModel: EventViewModel,
    eventTemplateViewModel: EventTemplateViewModel,
    shareAppViewModel: SharedAppViewModel,
    paddingValues: PaddingValues
) {
    composable(route = Routes.EventsPage.route) {

        LaunchedEffect(Unit) {
            eventViewModel.notificationEvent.collect { event -> eventViewModel.onNotification(event)}
        }

        val eventUiState = eventViewModel.uiState.collectAsStateWithLifecycle()
        val templateUiState = eventTemplateViewModel.uiState.collectAsStateWithLifecycle()
        val sharedAppUiState = shareAppViewModel.uiState.collectAsStateWithLifecycle()

        val formFields = eventViewModel.formUiState.collectAsStateWithLifecycle()
        val currentTime = eventViewModel.currentTime.collectAsStateWithLifecycle()


        EventScreen(
            uiState = eventUiState.value,
            templates = templateUiState.value.templates,
            onAction = { action -> eventViewModel.onAction(action) },
            eventFormFields = formFields.value,
            onFormAction = { action -> eventViewModel.onFormAction(action) },
            currentTime = currentTime.value,
            sharedAppUiState = sharedAppUiState.value,
            onSharedAction = {action -> shareAppViewModel.onAction(action)},
            paddingValues = paddingValues
        )
    }
}

fun NavGraphBuilder.templateScreen(
    eventTemplateViewModel: EventTemplateViewModel,
    paddingValues: PaddingValues
) {
    composable(route = Routes.TemplatePage.route) {

        val uiState = eventTemplateViewModel.uiState.collectAsState()
        val formUiState = eventTemplateViewModel.formUiState.collectAsState()

        LaunchedEffect(Unit) {
            eventTemplateViewModel.notificationEvent.collect { event -> eventTemplateViewModel.onNotification(event)}
        }

        TemplateScreen(
            uiState = uiState.value,
            onAction = { action -> eventTemplateViewModel.onAction(action)},
            formUiState= formUiState.value,
            onFormAction = { action -> eventTemplateViewModel.onFormAction(action) },
            paddingValues = paddingValues)
    }
}

fun NavGraphBuilder.settingsScreen(
    settingsViewModel: SettingsViewModel,
    paddingValues: PaddingValues
) {
    composable(route = Routes.SettingsPage.route) {

        val uiState = settingsViewModel.uiState.collectAsState()
        val formUiState = settingsViewModel.formUiState.collectAsState()

        LaunchedEffect(Unit) {
            settingsViewModel.notificationEvent.collect { event -> settingsViewModel.onNotification(event)}
        }

        SettingsScreen(
            uiState = uiState.value,
            onAction = { action -> settingsViewModel.onAction(action) },
            formUiState = formUiState.value,
            paddingValues = paddingValues
        )
    }
}

// region Функции навигации(Переходы)
fun navigateToHome(navController: NavController) {
    navController.navigate(Routes.HomePage.route)
}

fun navigateToInfo(navController: NavController) {
    navController.navigate(Routes.InfoPage.route)
}

fun navigateToEvent(navController: NavController) {
    navController.navigate(Routes.EventsPage.route)
}

fun navigateToTemplate(navController: NavController) {
    navController.navigate(Routes.TemplatePage.route)
}

fun navigateToSettings(navController: NavController) {
    navController.navigate(Routes.SettingsPage.route)
}

// endregion