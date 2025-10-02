/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.chartboost.adpreviewapp.R

@Composable
fun LauncherScreen(
    onLoggedIn: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val viewModel: LauncherViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(state) {
        when (state) {
            LauncherState.LoggedIn -> onLoggedIn()
            LauncherState.LoggedOut -> onLoggedOut()
            else -> Unit
        }
    }

    val imageResId =
        if (isDarkTheme) {
            R.drawable.chartboost_logo_dark_theme
        } else {
            R.drawable.chartboost_logo_light_theme
        }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = stringResource(R.string.chartboost_logo),
            modifier = Modifier.fillMaxWidth(0.75f),
        )
    }
}
