package com.example.kotlinclient.presentation.overlay

sealed interface OverlayScreen {
    data object Menu : OverlayScreen
    data object Events : OverlayScreen
    data object Create : OverlayScreen
}

data class OverlayUiState(
    val screen: OverlayScreen = OverlayScreen.Menu,
    val isExpanded: Boolean = false
)