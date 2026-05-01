/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.chartboost.adpreviewapp.ui.navigation.AppsDestination
import com.chartboost.adpreviewapp.ui.navigation.LoginDestination

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    navController: NavHostController,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.requestSystemSavedCredentials()
    }

    NavigateToAppsListEffect(uiState) {
        navController.navigate(AppsDestination) {
            popUpTo(LoginDestination) { inclusive = true }
        }
    }

    LoginScreen(
        email = uiState.email,
        password = uiState.password,
        isLoginEnabled = uiState.isLoginEnabled,
        loginState = uiState.loginState,
        isEmailValid = uiState.isEmailValid,
        errorType = uiState.errorType,
        onEmailValueChange = viewModel::onEmailUpdated,
        onPasswordValueChange = viewModel::onPasswordUpdated,
        onLoginClick = {
            viewModel.loginWithCredentials(uiState.email, uiState.password)
        },
    )
}

@Composable
private fun NavigateToAppsListEffect(
    uiState: LoginUiState,
    action: () -> Unit,
) {
    LaunchedEffect(uiState.loginState) {
        when (uiState.loginState) {
            LoginState.LoggedIn -> action()
            else -> {}
        }
    }
}
