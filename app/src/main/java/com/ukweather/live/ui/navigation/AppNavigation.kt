package com.ukweather.live.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ukweather.live.ui.screens.splash.SplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import com.ukweather.live.ui.screens.settings.SettingsViewModel
import androidx.compose.runtime.LaunchedEffect
import com.ukweather.live.ui.screens.home.HomeScreen
import com.ukweather.live.ui.screens.cities.CitiesScreen
import com.ukweather.live.ui.screens.settings.SettingsScreen
import com.ukweather.live.ui.screens.radar.RadarScreen
import com.ukweather.live.ui.screens.airquality.AirQualityDetailsScreen
import com.ukweather.live.ui.screens.favorites.FavoritesScreen

import com.ukweather.live.R

sealed class Screen(val route: String, val titleResId: Int, val icon: @Composable () -> Unit) {
    object Home : Screen("home", R.string.nav_home, { Icon(Icons.Outlined.Home, contentDescription = null) })
    object Favorites : Screen("favorites", R.string.nav_favorites, { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) })
    object Cities : Screen("cities", R.string.nav_cities, { Icon(Icons.Outlined.LocationOn, contentDescription = null) })
    object Radar : Screen("radar", R.string.nav_radar, { Icon(Icons.AutoMirrored.Outlined.List, contentDescription = null) })
    object Settings : Screen("settings", R.string.nav_settings, { Icon(Icons.Outlined.Settings, contentDescription = null) })
    object Splash : Screen("splash", R.string.nav_home, { }) // Splash doesn't show in nav
    object AirQuality : Screen("air_quality", R.string.nav_home, { }) 
    object AdvancedDetails : Screen("advanced_details", R.string.nav_home, { })
}

@Composable
fun AppNavigation(
    adViewModel: com.ukweather.live.util.AdViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val adManager = adViewModel.adManager
    val navController = rememberNavController()
    val context = LocalContext.current
    val isPremium by settingsViewModel.isPremium.collectAsState()
    
    LaunchedEffect(Unit) {
        if (!isPremium) {
            adManager.loadInterstitial()
        }
    }
    
    val items = listOf(
        Screen.Home,
        Screen.Favorites,
        Screen.Cities,
        Screen.Radar,
        Screen.Settings
    )

    val weatherViewModel: com.ukweather.live.ui.screens.home.WeatherViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            Column {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = screen.icon,
                            label = { Text(stringResource(screen.titleResId)) },
                            selected = currentDestination?.hierarchy?.any { 
                                it.route == screen.route
                            } == true,
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                val navigate = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                
                                if (!isPremium && screen.route != currentDestination?.route) {
                                    adManager.showInterstitial(context as Activity) {
                                        navigate()
                                    }
                                } else {
                                    navigate()
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) },
            exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)) },
            popEnterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) },
            popExitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)) }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    viewModel = weatherViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) { 
                HomeScreen(
                    viewModel = weatherViewModel,
                    onAirQualityClick = {
                        navController.navigate(Screen.AirQuality.route)
                    },
                    onAdvancedDetailsClick = {
                        navController.navigate(Screen.AdvancedDetails.route)
                    }
                )
            }
            
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    weatherViewModel = weatherViewModel,
                    onCityClick = {
                        val navigate = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        
                        if (!isPremium) {
                            adManager.showInterstitial(context as Activity) {
                                navigate()
                            }
                        } else {
                            navigate()
                        }
                    }
                )
            }
            
            composable(Screen.AdvancedDetails.route) {
                val uiState by weatherViewModel.uiState.collectAsState()
                val successState = uiState as? com.ukweather.live.ui.screens.home.WeatherUiState.Success
                
                if (successState != null) {
                    com.ukweather.live.ui.screens.advanced.AdvancedDetailsScreen(
                        cityName = successState.cityName,
                        weather = successState.weather,
                        airQuality = successState.airQuality,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            
            composable(Screen.AirQuality.route) {
                val uiState by weatherViewModel.uiState.collectAsState()
                val successState = uiState as? com.ukweather.live.ui.screens.home.WeatherUiState.Success
                
                AirQualityDetailsScreen(
                    cityName = successState?.cityName ?: "Unknown",
                    weather = successState?.weather,
                    airQuality = successState?.airQuality,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Cities.route) { 
                CitiesScreen(
                    weatherViewModel = weatherViewModel,
                    onCityClick = {
                        val navigate = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        
                        if (!isPremium) {
                            adManager.showInterstitial(context as Activity) {
                                navigate()
                            }
                        } else {
                            navigate()
                        }
                    }
                ) 
            }
            composable(Screen.Radar.route) { RadarScreen(viewModel = weatherViewModel) }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}



