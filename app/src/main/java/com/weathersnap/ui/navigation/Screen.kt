package com.weathersnap.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Weather : Screen("weather")
    data object Camera : Screen("camera")
    data object SavedReports : Screen("saved_reports")
    data object CreateReport : Screen("create_report/{weatherJson}") {
        const val ARG_WEATHER_JSON = "weatherJson"

        fun createRoute(weatherJson: String): String {
            return "create_report/${Uri.encode(weatherJson)}"
        }
    }
}
