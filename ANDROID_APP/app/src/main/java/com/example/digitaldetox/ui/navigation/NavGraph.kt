package com.example.digitaldetox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.example.digitaldetox.data.preferences.UserPreferencesRepository
import com.example.digitaldetox.ui.home.HomeScreen
import com.example.digitaldetox.ui.home.HomeViewModel
import com.example.digitaldetox.ui.home.HomeViewModelFactory
import com.example.digitaldetox.ui.apps.AppsScreen
import com.example.digitaldetox.ui.detox.DetoxScreen
import com.example.digitaldetox.ui.analytics.AnalyticsScreen
import com.example.digitaldetox.ui.settings.SettingsScreen
import com.example.digitaldetox.ui.onboarding.OnboardingScreen

import com.example.digitaldetox.data.local.datastore.AppSettings

@Composable
fun DettoNavGraph(
    userPreferencesRepository: UserPreferencesRepository? = null,
    appRepository: com.example.digitaldetox.data.repository.AppRepository? = null,
    appSettings: AppSettings? = null,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                repository = userPreferencesRepository,
                onFinish = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            val context = LocalContext.current
            val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = if (appRepository != null && appSettings != null) {
                    HomeViewModelFactory(appRepository, appSettings, context)
                } else {
                    throw IllegalStateException("AppRepository or AppSettings not initialized")
                }
            )
            HomeScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }, viewModel = viewModel)
        }
        composable("apps") {
            AppsScreen(
                repository = appRepository,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable("detox") {
            DetoxScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        composable(
            route = "analytics",
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "detto://analytics" })
        ) {
            AnalyticsScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                appRepository = appRepository,
                appSettings = appSettings
            )
        }
        composable("settings") {
            SettingsScreen(
                appSettings = appSettings,
                onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
    }
}
