package com.example.kotlinclient.presentation.navigation

import android.content.Context
import android.widget.Toast
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
import com.example.kotlinclient.state_management.viewModel.EventFormAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateViewModel
import com.example.kotlinclient.state_management.viewModel.EventViewModel
import com.example.kotlinclient.state_management.viewModel.HomeAction
import com.example.kotlinclient.state_management.viewModel.HomeViewModel
import com.example.kotlinclient.state_management.viewModel.InfoAction
import com.example.kotlinclient.state_management.viewModel.InfoViewModel
import com.example.kotlinclient.state_management.viewModel.SettingsAction
import com.example.kotlinclient.state_management.viewModel.SettingsViewModel
import com.example.kotlinclient.state_management.viewModel.SharedAction
import com.example.kotlinclient.state_management.viewModel.SharedAppViewModel
import com.example.kotlinclient.state_management.viewModel.ValidationEvent

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
            onAction = { action ->
                when (action) {
                    is InfoAction.SelectType -> {
                        infoViewModel.onAction(InfoAction.SelectType(action.id))
                    }

                    is InfoAction.ChangeSearchQuery -> {
                        infoViewModel.onAction(InfoAction.ChangeSearchQuery(action.query))
                    }

                    is InfoAction.ClearQuery -> {
                        infoViewModel.onAction(InfoAction.ClearQuery)
                    }

                    is InfoAction.UpdateContentPin -> {
                        infoViewModel.onAction(
                            InfoAction.UpdateContentPin(
                                action.id,
                                action.pinStatus
                            )
                        )
                    }
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
) {
    composable(route = Routes.EventsPage.route) {

        val context = LocalContext.current

        LaunchedEffect(Unit) {
            eventViewModel.validationEvents.collect { event ->
                when (event) {
                    is ValidationEvent.EmptyName -> eventViewModel.onValidation(
                        ValidationEvent.EmptyName(
                            context
                        )
                    )

                    is ValidationEvent.EmptyTime -> eventViewModel.onValidation(
                        ValidationEvent.EmptyTime(
                            context
                        )
                    )

                    is ValidationEvent.InvalidTime -> eventViewModel.onValidation(
                        ValidationEvent.InvalidTime(
                            context
                        )
                    )

                    is ValidationEvent.SuccessCreate -> eventViewModel.onValidation(
                        ValidationEvent.SuccessCreate(
                            context
                        )
                    )

                    is ValidationEvent.SuccessUpdate -> eventViewModel.onValidation(
                        ValidationEvent.SuccessUpdate(
                            context
                        )
                    )
                }
            }
        }

        val eventUiState = eventViewModel.uiState.collectAsStateWithLifecycle()
        val templateUiState = eventTemplateViewModel.uiState.collectAsStateWithLifecycle()

        val formFields = eventViewModel.eventFormFields.collectAsStateWithLifecycle()
        val currentTime = eventViewModel.currentTime.collectAsStateWithLifecycle()


        EventScreen(
            uiState = eventUiState.value,
            templates = templateUiState.value.templates,
            onAction = { action ->
                when (action) {
                    is EventAction.DeleteEvent -> eventViewModel.onAction(
                        EventAction.DeleteEvent(
                            action.id
                        )
                    )

                    is EventAction.DismissDialog -> eventViewModel.onAction(EventAction.DismissDialog)
                    is EventAction.OpenDialog -> eventViewModel.onAction(
                        EventAction.OpenDialog(
                            action.dialog
                        )
                    )
                }
            },
            eventFormFields = formFields.value,
            onFormAction = { action ->
                when (action) {
                    is EventFormAction.SelectTemplate -> eventViewModel.onFormAction(
                        EventFormAction.SelectTemplate(
                            action.template
                        )
                    )

                    is EventFormAction.ValidateAndSave -> eventViewModel.onFormAction(
                        EventFormAction.ValidateAndSave(action.context)
                    )

                    is EventFormAction.UpdateEndTime -> eventViewModel.onFormAction(EventFormAction.UpdateEndTime)
                    is EventFormAction.LoadUiState -> eventViewModel.onFormAction(
                        EventFormAction.LoadUiState(
                            action.event
                        )
                    )

                    is EventFormAction.ClearUiState -> eventViewModel.onFormAction(EventFormAction.ClearUiState)
                }
            },
            currentTime = currentTime.value,
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

        TemplateScreen(templates = uiState.value.templates, onAction = { action ->
            when (action) {
                is EventTemplateAction.DeleteTemplate -> eventTemplateViewModel.onAction(
                    EventTemplateAction.DeleteTemplate(
                        action.id
                    )
                )
            }
        }, paddingValues = paddingValues)
    }
}

fun NavGraphBuilder.settingsScreen(
    settingsViewModel: SettingsViewModel,
    paddingValues: PaddingValues
) {
    composable(route = Routes.SettingsPage.route) {

        val uiState = settingsViewModel.uiState.collectAsState()

        SettingsScreen(
            uiState = uiState.value,
            onAction = { action ->
                when (action) {
                    is SettingsAction.SwitchPreference -> settingsViewModel.onAction(
                        SettingsAction.SwitchPreference(
                            action.key
                        )
                    )
                    is SettingsAction.SetUserId -> settingsViewModel.onAction(
                        SettingsAction.SetUserId(
                            action.id
                        )
                    )
                    is SettingsAction.EditUserInfo -> settingsViewModel.onAction(
                        SettingsAction.EditUserInfo(
                            action.login,
                            action.email
                        )
                    )
                    is SettingsAction.ExitProfile -> settingsViewModel.onAction(SettingsAction.ExitProfile)
                }
            },
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