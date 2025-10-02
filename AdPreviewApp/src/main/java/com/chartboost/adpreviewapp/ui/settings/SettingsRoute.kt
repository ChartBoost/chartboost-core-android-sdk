/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.ui.navigation.AppsDestination
import com.chartboost.adpreviewapp.ui.navigation.LoginDestination
import com.chartboost.adpreviewapp.ui.utils.ShowToastEffect

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    navController: NavController,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val errorMessage =
        if (uiState.showTestModeErrorMessage) {
            stringResource(R.string.switch_test_mode_internal_error_message)
        } else {
            null
        }

    ShowToastEffect(errorMessage, viewModel::onToastShown)

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navController.navigate(LoginDestination) {
                popUpTo(AppsDestination) { inclusive = true }
            }
            viewModel.onLogoutHandled()
        }
    }

    val deleteAccountUrl = stringResource(R.string.delete_account_url)

    SettingsScreen(
        onBackClicked = { navController.popBackStack() },
        onTestModeChecked = viewModel::onTestModeToggled,
        onClearLogsChecked = viewModel::onClearLogsToggled,
        showLmtStatus = uiState.showLmtStatus,
        clearLogsOnLoad = uiState.clearLogsOnLoad,
        isTestMode = uiState.isTestMode,
        lmtStatus = uiState.lmtStatus,
        onDeleteAccountClicked = {
            openBrowser(deleteAccountUrl, context)
        },
        onLogOutClicked = { viewModel.logout() },
        monetizationSdkVersion = uiState.monetizationSdkVersion,
        mediationSdkVersion = uiState.mediationSdkVersion,
    )
}

private fun openBrowser(
    url: String,
    context: Context,
) {
    val intent =
        Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK +
                Intent.FLAG_ACTIVITY_CLEAR_TOP +
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

    context.startActivity(intent)
}
