package com.example.kotlinclient.presentation.navigation

// Перечисление путей к экранам
enum class Routes(var route:String) {
    HomePage(route="home"),
    InfoPage(route="info"),
    EventsPage(route="event"),
    TemplatePage(route="template"),
    SettingsPage(route="settings")
}