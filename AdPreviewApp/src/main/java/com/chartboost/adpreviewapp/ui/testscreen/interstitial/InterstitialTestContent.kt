/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen.interstitial

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.ui.testscreen.CallbackChipState
import com.chartboost.adpreviewapp.ui.theme.ActivatedChipColor
import com.chartboost.adpreviewapp.ui.theme.AdPreviewAppTheme
import com.chartboost.adpreviewapp.ui.theme.CbGray
import com.chartboost.adpreviewapp.ui.theme.CbRedPrimary
import com.chartboost.adpreviewapp.ui.theme.Dimens
import com.chartboost.adpreviewapp.ui.theme.ErrorChipColor
import com.chartboost.adpreviewapp.ui.utils.AppTopBar
import com.chartboost.adpreviewapp.ui.utils.EventLogViewer
import com.chartboost.adpreviewapp.ui.utils.LoadingButton
import com.chartboost.adpreviewapp.ui.utils.PrimaryButton
import com.chartboost.adpreviewapp.ui.utils.model.LoadingButtonState

@Composable
fun InterstitialTestContent(
    uiState: InterstitialTestUiState,
    onBackClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onLoadFullscreenAd: (Context) -> Unit,
    onShowFullscreenAd: (Activity?) -> Unit,
    onClearAd: () -> Unit,
    onKeywordsCLick: () -> Unit,
    eventLog: List<String>,
    onClearLogs: () -> Unit,
) {
    val context = LocalContext.current
    Scaffold(topBar = {
        AppTopBar(
            title = {
                Text(uiState.adTitle ?: stringResource(R.string.ad_location_test_screen_title))
            },
            showBackButton = true,
            onBack = onBackClicked,
            showSettingsButton = true,
            onSettingsClick = onSettingsClicked,
        )
    }) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.paddingM),
        ) {
            ButtonsSections(
                uiState = uiState,
                onLoadCLick = { onLoadFullscreenAd(context) },
                onShowClick = { onShowFullscreenAd(context as? Activity) },
                onClearCLick = onClearAd,
                onKeywordsCLick = onKeywordsCLick,
            )

            CallbackChipGroup(chipsMap = uiState.callbackChipMap)

            val errorMessageString =
                when (val errorMessage = uiState.errorMessage) {
                    is InterstitialScreenErrorMessage.LoadFailedErrorMessage ->
                        stringResource(
                            R.string.loading_interstitial_failed,
                            errorMessage.error,
                        )
                    is InterstitialScreenErrorMessage.ShowFailedErrorMessage ->
                        stringResource(
                            R.string.showing_interstitial_failed,
                            errorMessage.error,
                        )
                    InterstitialScreenErrorMessage.NoLocationNameErrorMessage ->
                        stringResource(
                            R.string.internal_app_error_no_location_name,
                        )
                    InterstitialScreenErrorMessage.ShowFailedNoActivityErrorMessage ->
                        stringResource(
                            R.string.internal_app_problem_no_activity_to_show_ad,
                        )
                    null -> null
                }
            if (errorMessageString != null) {
                Text(
                    text = errorMessageString,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            EventLogViewer(
                logs = eventLog,
                onClearClicked = onClearLogs,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
            )
        }
    }
}

@Composable
private fun ButtonsSections(
    uiState: InterstitialTestUiState,
    onLoadCLick: () -> Unit = {},
    onShowClick: () -> Unit = {},
    onClearCLick: () -> Unit = {},
    onKeywordsCLick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingS),
        modifier = Modifier.fillMaxWidth(),
    ) {
        LoadingButton(
            onClick = onLoadCLick,
            text = "Load",
            state = uiState.loadingButtonState,
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            enabled = uiState.isShowBtnEnabled,
            onClick = onShowClick,
            text = "Show",
            modifier = Modifier.weight(1f),
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingS),
        modifier = Modifier.fillMaxWidth(),
    ) {
        PrimaryButton(
            enabled = uiState.isClearBtnEnabled,
            onClick = onClearCLick,
            text = "Clear",
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            onClick = onKeywordsCLick,
            text = "Keywords (${uiState.numberOfKeywords})",
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallbackChipGroup(chipsMap: Map<String, CallbackChipState>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingS),
        modifier = Modifier.padding(vertical = Dimens.paddingM),
    ) {
        chipsMap.forEach { entry ->
            CallbackChip(label = entry.key, state = entry.value)
        }
    }
}

@Composable
fun CallbackChip(
    label: String,
    state: CallbackChipState,
) {
    when (state) {
        CallbackChipState.TRIGGERED -> {
            SuggestionChip(
                onClick = {},
                label = { Text(label) },
                enabled = false,
                colors = ActivatedChipColor,
                border = BorderStroke(Dimens.borderStroke, color = CbRedPrimary),
            )
        }

        CallbackChipState.ERROR -> {
            SuggestionChip(
                onClick = {},
                label = { Text(label) },
                enabled = false,
                colors = ErrorChipColor,
                border = BorderStroke(width = Dimens.borderStroke, color = CbGray),
            )
        }

        CallbackChipState.NOT_TRIGGERED -> {
            SuggestionChip(
                onClick = {},
                label = { Text(label) },
                enabled = false,
            )
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
fun InterstitialTestScreenContentLightPreview() {
    val mockUiState =
        InterstitialTestUiState(
            adTitle = "Interstitial Test",
            loadingButtonState = LoadingButtonState.LOADING,
            isShowBtnEnabled = false,
            isClearBtnEnabled = true,
            numberOfKeywords = 5,
            callbackChipMap =
                mapOf(
                    "onAdClicked" to CallbackChipState.TRIGGERED,
                    "onAdClosed" to CallbackChipState.ERROR,
                    "onAdRewarded" to CallbackChipState.NOT_TRIGGERED,
                    "onAdImpressionRecorded" to CallbackChipState.NOT_TRIGGERED,
                    "onAdExpired" to CallbackChipState.NOT_TRIGGERED,
                ),
            errorMessage = null,
        )
    AdPreviewAppTheme {
        InterstitialTestContent(
            uiState = mockUiState,
            onBackClicked = {},
            onSettingsClicked = {},
            onLoadFullscreenAd = {},
            onShowFullscreenAd = {},
            onClearAd = {},
            onKeywordsCLick = {},
            eventLog = listOf("onAdLoaded", "onAdClicked", "loadSucceeded"),
            onClearLogs = {},
        )
    }
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun InterstitialTestScreenContentDarkPreview() {
    val mockUiState =
        InterstitialTestUiState(
            adTitle = "Interstitial Test",
            loadingButtonState = LoadingButtonState.ENABLED,
            isShowBtnEnabled = false,
            isClearBtnEnabled = false,
            numberOfKeywords = 3,
            callbackChipMap =
                mapOf(
                    "onAdClicked" to CallbackChipState.TRIGGERED,
                    "onAdClosed" to CallbackChipState.ERROR,
                    "onAdRewarded" to CallbackChipState.NOT_TRIGGERED,
                    "onAdImpressionRecorded" to CallbackChipState.NOT_TRIGGERED,
                    "onAdExpired" to CallbackChipState.NOT_TRIGGERED,
                ),
            errorMessage = InterstitialScreenErrorMessage.ShowFailedNoActivityErrorMessage,
        )
    AdPreviewAppTheme {
        InterstitialTestContent(
            uiState = mockUiState,
            onBackClicked = {},
            onSettingsClicked = {},
            onLoadFullscreenAd = {},
            onShowFullscreenAd = {},
            onClearAd = {},
            onKeywordsCLick = {},
            eventLog = listOf("onAdLoaded", "onAdClicked", "loadSucceeded"),
            onClearLogs = {},
        )
    }
}
