/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.domain.sdk.AdType
import com.chartboost.adpreviewapp.ui.apps.AppsScreen
import com.chartboost.adpreviewapp.ui.keywords.KeywordsScreen
import com.chartboost.adpreviewapp.ui.launcher.LauncherScreen
import com.chartboost.adpreviewapp.ui.locations.AdLocationsScreen
import com.chartboost.adpreviewapp.ui.login.LoginRoute
import com.chartboost.adpreviewapp.ui.settings.SettingsRoute
import com.chartboost.adpreviewapp.ui.testscreen.banner.BannerTestScreen
import com.chartboost.adpreviewapp.ui.testscreen.interstitial.InterstitialTestScreen
import com.chartboost.adpreviewapp.ui.utils.ShowToastEffect

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = LauncherDestination,
    ) {
        composable<LauncherDestination> {
            LauncherScreen(
                onLoggedIn = {
                    navController.navigate(AppsDestination) {
                        popUpTo(LauncherDestination) { inclusive = true }
                    }
                },
                onLoggedOut = {
                    navController.navigate(LoginDestination) {
                        popUpTo(LauncherDestination) { inclusive = true }
                    }
                },
            )
        }

        composable<LoginDestination> {
            LoginRoute(
                viewModel = hiltViewModel(),
                navController = navController,
            )
        }

        composable<AppsDestination> {
            AppsScreen(
                onSettingsClicked = { navController.navigate(SettingsDestination) },
                onAppSelected = { appId -> navController.navigate(AdLocationsDestination(appId)) },
            )
        }

        composable<AdLocationsDestination> {
            AdLocationsScreen(
                onLocationClicked = { locationName, adType ->
                    navController.navigate(
                        AdLocationTestDestination(
                            locationName,
                            adType,
                        ),
                    )
                },
                onSettingsClicked = { navController.navigate(SettingsDestination) },
            )
        }

        composable<AdLocationTestDestination> { backStackEntry ->
            val args = backStackEntry.toRoute<AdLocationTestDestination>()

            when (args.adType) {
                AdType.BANNER.serverName ->
                    BannerTestScreen(
                        onSettingsClicked = { navController.navigate(SettingsDestination) },
                        onBackClicked = { navController.popBackStack() },
                        onKeywordsClicked = { navController.navigate(KeywordsDestination) },
                    )

                AdType.INTERSTITIAL.serverName, AdType.REWARDED.serverName ->
                    InterstitialTestScreen(
                        onSettingsClicked = { navController.navigate(SettingsDestination) },
                        onBackClicked = { navController.popBackStack() },
                        onKeywordsCLick = { navController.navigate(KeywordsDestination) },
                    )

                else -> {
                    // Show toast message and display ad locations screen
                    ShowToastEffect(
                        message =
                            stringResource(
                                R.string.unknown_type_of_ad,
                                args.adType,
                            ),
                    ) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<KeywordsDestination> {
            KeywordsScreen(
                onBackClicked = { navController.popBackStack() },
            )
        }

        composable<SettingsDestination> {
            SettingsRoute(
                viewModel = hiltViewModel(),
                navController = navController,
            )
        }
    }
}
