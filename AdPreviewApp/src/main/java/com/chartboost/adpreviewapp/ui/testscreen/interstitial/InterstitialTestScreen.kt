/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen.interstitial

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun InterstitialTestScreen(
    onBackClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onKeywordsCLick: () -> Unit,
    viewModel: InterstitialTestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val eventLog by viewModel.eventLog.collectAsState()

    InterstitialTestContent(
        uiState = uiState,
        onBackClicked = onBackClicked,
        onSettingsClicked = onSettingsClicked,
        onLoadFullscreenAd = { context -> viewModel.loadFullscreenAd(context) },
        onShowFullscreenAd = { activity: Activity? -> viewModel.showFullscreenAd(activity) },
        onClearAd = viewModel::clearAd,
        onKeywordsCLick = onKeywordsCLick,
        eventLog = eventLog,
        onClearLogs = viewModel::clearEventLogs,
    )
}
