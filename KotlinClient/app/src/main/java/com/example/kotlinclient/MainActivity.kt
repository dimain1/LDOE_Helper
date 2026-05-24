package com.example.kotlinclient

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.entity.UserEntity
import com.example.kotlinclient.presentation.AppHeader
import com.example.kotlinclient.presentation.MyBottomAppBar
import com.example.kotlinclient.presentation.event.modal.EventViewDetails
import com.example.kotlinclient.presentation.home.HomeScreen
import com.example.kotlinclient.presentation.navigation.ApplicationNavHost
import com.example.kotlinclient.presentation.navigation.Routes
import com.example.kotlinclient.presentation.navigation.navigateToEvent
import com.example.kotlinclient.presentation.navigation.navigateToHome
import com.example.kotlinclient.presentation.navigation.navigateToInfo
import com.example.kotlinclient.presentation.navigation.navigateToSettings
import com.example.kotlinclient.presentation.navigation.navigateToTemplate
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import com.example.kotlinclient.state_management.viewModel.DialogType
import com.example.kotlinclient.state_management.viewModel.EventViewModel
import com.example.kotlinclient.state_management.viewModel.SharedAction
import com.example.kotlinclient.state_management.viewModel.SharedAppViewModel
import com.example.kotlinclient.ui.theme.KotlinClientTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    val LocalUserSession = staticCompositionLocalOf<UserSession> {
        error("UserSession not provided")
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val userSession: UserSession = koinInject()
            val sharedAppViewModel: SharedAppViewModel = koinViewModel()
            val eventViewModel: EventViewModel = koinViewModel()

            val sharedUiState = sharedAppViewModel._uiState.collectAsState()
            val eventDetailsDialogUiState = sharedAppViewModel._eventDetailsDialogUiState.collectAsState()

            // Глобальное состояние для userSession
            CompositionLocalProvider(LocalUserSession provides userSession){
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

                        if(eventDetailsDialogUiState.value.showDialog) {
                            EventViewDetails(onAction = { action -> eventViewModel.onAction(action) }, onDismiss = {
                                sharedAppViewModel.onAction(SharedAction.ChangeDialogVisibility(false,
                                    DialogType.EventViewDetailsDialog(null)))
                            }, eventDetailsDialogUiState.value.initialData)
                        }
                        ApplicationNavHost(navController, startDestination,paddingValues, sharedAppViewModel)
                    }
                }
            }
        }
    }
}

