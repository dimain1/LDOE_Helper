package com.example.kotlinclient.presentation.navigation

// Перечисление путей к экранам
enum class Routes(var route:String) {
    HomePage(route="Home"),
    InfoPage(route="Database"),
    EventsPage(route="Events"),
    TemplatePage(route="Templates"),
    SettingsPage(route="Settings")
}