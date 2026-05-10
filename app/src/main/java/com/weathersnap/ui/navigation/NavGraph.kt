package com.weathersnap.ui.navigation

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.weathersnap.domain.model.Weather
import com.weathersnap.ui.camera.CameraScreen
import com.weathersnap.ui.report.CreateReportScreen
import com.weathersnap.ui.savedreports.SavedReportsScreen
import com.weathersnap.ui.weather.WeatherScreen

@Composable
fun WeatherNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Weather.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable(Screen.Weather.route) {
            WeatherScreen(
                onCreateReport = { weather ->
                    navController.navigate(Screen.CreateReport.createRoute(Gson().toJson(weather)))
                },
                onReports = { navController.navigate(Screen.SavedReports.route) }
            )
        }
        composable(
            route = Screen.CreateReport.route,
            arguments = listOf(navArgument(Screen.CreateReport.ARG_WEATHER_JSON) { type = NavType.StringType })
        ) { entry ->
            val encodedJson = entry.arguments?.getString(Screen.CreateReport.ARG_WEATHER_JSON).orEmpty()
            val weather = Gson().fromJson(Uri.decode(encodedJson), Weather::class.java)
            CreateReportScreen(
                navController = navController,
                weather = weather,
                onBack = { navController.popBackStack() },
                onCapture = { navController.navigate(Screen.Camera.route) },
                onSaved = {
                    navController.navigate(Screen.SavedReports.route) {
                        popUpTo(Screen.Weather.route)
                    }
                }
            )
        }
        composable(Screen.Camera.route) {
            CameraScreen(navController = navController)
        }
        composable(Screen.SavedReports.route) {
            SavedReportsScreen(
                onBack = { navController.popBackStack() },
                onWeatherSearch = { navController.popBackStack(Screen.Weather.route, inclusive = false) }
            )
        }
    }
}
