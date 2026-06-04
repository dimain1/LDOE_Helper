package com.example.kotlinclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kotlinclient.presentation.AppHeader
import com.example.kotlinclient.presentation.MyBottomAppBar
import com.example.kotlinclient.presentation.event.modal.EventViewDetails
import com.example.kotlinclient.presentation.info.modal.GameContentViewDetails
import com.example.kotlinclient.presentation.navigation.ApplicationNavHost
import com.example.kotlinclient.presentation.navigation.Routes
import com.example.kotlinclient.presentation.navigation.navigateToEvent
import com.example.kotlinclient.presentation.navigation.navigateToHome
import com.example.kotlinclient.presentation.navigation.navigateToInfo
import com.example.kotlinclient.presentation.navigation.navigateToSettings
import com.example.kotlinclient.presentation.navigation.navigateToTemplate
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.viewModel.DialogType
import com.example.kotlinclient.state_management.viewModel.EventAction
import com.example.kotlinclient.state_management.viewModel.EventViewModel
import com.example.kotlinclient.state_management.viewModel.InfoAction
import com.example.kotlinclient.state_management.viewModel.InfoViewModel
import com.example.kotlinclient.state_management.viewModel.SharedAction
import com.example.kotlinclient.state_management.viewModel.SharedAppViewModel
import com.example.kotlinclient.ui.theme.KotlinClientTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    val LocalUserSession = staticCompositionLocalOf<UserSessionProvider> {
        error("UserSession not provided")
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {


                val userSession: UserSessionProvider = koinInject()
                val sharedAppViewModel: SharedAppViewModel = koinViewModel()
                val eventViewModel: EventViewModel = koinViewModel()
                val infoViewModel: InfoViewModel = koinViewModel()

                val sharedUiState = sharedAppViewModel.uiState.collectAsState()

                // Глобальное состояние для userSession
                CompositionLocalProvider(LocalUserSession provides userSession) {
                    // Основная тема приложения определяющая типографию и Цветовые схемы
                    KotlinClientTheme(sharedUiState.value.darkTheme) {

                        // Контроллер навигации осуществляющий переходы(Единственный экземпляр)
                        val navController = rememberNavController()

                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        // Путь к начальному экрану
                        val startDestination = Routes.HomePage.route
                        // Системная обёртка позволяющая получить информацию об системных оступах и их предусмотреть
                        Scaffold(
                            containerColor = colorScheme.primaryContainer,
                            topBar = { AppHeader() },
                            bottomBar = {
                                MyBottomAppBar(
                                    currentRoute,
                                    { navigateToHome(navController) },
                                    { navigateToInfo(navController) },
                                    { navigateToEvent(navController) },
                                    { navigateToTemplate(navController) },
                                    { navigateToSettings(navController) },
                                )
                            }
                        ) { paddingValues ->

                            if (sharedUiState.value.eventDetailsDialogUiState.initialData != null
                                && sharedUiState.value.eventDetailsDialogUiState.showDialog
                            ) {
                                EventViewDetails(
                                    onAction = { action -> eventViewModel.onAction(action) },
                                    onDismiss = {
                                        eventViewModel.onAction(EventAction.DismissDialog)
                                        sharedAppViewModel.onAction(
                                            SharedAction.ChangeDialogVisibility(
                                                false,
                                                DialogType.EventViewDetailsDialog(null)
                                            )
                                        )
                                    },
                                    sharedUiState.value.eventDetailsDialogUiState.initialData
                                )
                            }

                            if (sharedUiState.value.gameContentDetailsDialogUiState.initialData != null
                                && sharedUiState.value.gameContentDetailsDialogUiState.showDialog
                            ) {
                                GameContentViewDetails(
                                    onDismiss = {
                                        sharedAppViewModel.onAction(
                                            SharedAction.ChangeDialogVisibility(
                                                false,
                                                DialogType.GameContentViewDetailsDialog(null)
                                            )
                                        )
                                        infoViewModel.onAction(InfoAction.DismissDialog)
                                    },
                                    initialData = sharedUiState.value.gameContentDetailsDialogUiState.initialData
                                )
                            }
                            ApplicationNavHost(
                                navController,
                                startDestination,
                                paddingValues,
                                sharedAppViewModel
                            )
                        }
                    }
                }
            }


    }
}

