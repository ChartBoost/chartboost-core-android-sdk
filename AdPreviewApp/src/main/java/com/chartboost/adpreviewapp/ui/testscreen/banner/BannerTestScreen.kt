/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen.banner

import android.content.Context
import android.content.res.Configuration
import android.widget.RelativeLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.ui.testscreen.CallbackChipState
import com.chartboost.adpreviewapp.ui.theme.ActivatedChipColor
import com.chartboost.adpreviewapp.ui.theme.AdPreviewAppTheme
import com.chartboost.adpreviewapp.ui.theme.CbGray
import com.chartboost.adpreviewapp.ui.theme.CbRedPrimary
import com.chartboost.adpreviewapp.ui.theme.Dimens
import com.chartboost.adpreviewapp.ui.theme.Dimens.bannerHeights
import com.chartboost.adpreviewapp.ui.theme.Dimens.bannerWidth
import com.chartboost.adpreviewapp.ui.theme.ErrorChipColor
import com.chartboost.adpreviewapp.ui.utils.AppTopBar
import com.chartboost.adpreviewapp.ui.utils.EventLogViewer
import com.chartboost.adpreviewapp.ui.utils.LoadingButton
import com.chartboost.adpreviewapp.ui.utils.PrimaryButton
import com.chartboost.adpreviewapp.ui.utils.model.LoadingButtonState

@Composable
fun BannerTestScreen(
    onBackClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onKeywordsClicked: () -> Unit,
    viewModel: BannerTestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val eventLog by viewModel.eventLog.collectAsState()

    BannerTestContent(
        uiState = uiState,
        onLoadClicked = { context -> viewModel.loadBanner(context) },
        onClearClicked = viewModel::clearBanner,
        onKeywordsClicked = onKeywordsClicked,
        onBackClicked = onBackClicked,
        onSettingsClicked = onSettingsClicked,
        eventLog = eventLog,
        onClearLogs = viewModel::clearEventLogs,
        onAndroidViewRelease = viewModel::onBannerReleased,
    )
}

@Composable
private fun BannerTestContent(
    uiState: BannerTestUiState,
    onLoadClicked: (Context) -> Unit,
    onClearClicked: () -> Unit,
    onKeywordsClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    eventLog: List<String>,
    onClearLogs: () -> Unit,
    onAndroidViewRelease: () -> Unit,
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
                onLoadClick = { onLoadClicked(context) },
                onClearClick = onClearClicked,
                onKeywordsClick = onKeywordsClicked,
            )

            CallbackChipGroup(chipsMap = uiState.callbackChipMap)

            val errorMessageString =
                when (val errorMessage = uiState.errorMessage) {
                    is BannerScreenErrorMessage.LoadFailedErrorMessage ->
                        stringResource(
                            R.string.loading_banner_failed,
                            errorMessage.error,
                        )

                    BannerScreenErrorMessage.NoLocationNameErrorMessage -> stringResource(R.string.internal_app_error_no_location_name)
                    null -> null
                }
            if (errorMessageString != null) {
                Text(
                    text = errorMessageString,
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = Dimens.paddingS),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
            ) {
                EventLogViewer(
                    logs = eventLog,
                    onClearClicked = onClearLogs,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                )

                Spacer(modifier = Modifier.height(Dimens.paddingM))

                uiState.banner?.let { banner ->
                    AndroidView(
                        factory = { context ->
                            RelativeLayout(
                                context,
                            ).apply {
                                layoutParams =
                                    RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    )
                            }
                        },
                        update = { relativeLayout ->
                            (banner.parent as? RelativeLayout)?.removeView(banner)
                            relativeLayout.addView(banner)
                        },
                        modifier =
                            Modifier
                                .padding(bottom = Dimens.paddingXL)
                                .width(bannerWidth)
                                .height(bannerHeights)
                                .align(Alignment.CenterHorizontally),
                        onRelease = {
                            onAndroidViewRelease()
                        },
                    )
                } ?: Box(
                    modifier =
                        Modifier
                            .padding(bottom = Dimens.paddingXL)
                            .width(bannerWidth)
                            .height(bannerHeights)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "The ad will be placed here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonsSections(
    uiState: BannerTestUiState,
    onLoadClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onKeywordsClick: () -> Unit = {},
) {
    LoadingButton(onClick = onLoadClick, text = "Load", state = uiState.loadingButtonState, modifier = Modifier.fillMaxWidth())

    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.paddingS), modifier = Modifier.fillMaxWidth()) {
        PrimaryButton(enabled = uiState.isClearBtnEnabled, onClick = onClearClick, text = "Clear", modifier = Modifier.weight(1f))
        PrimaryButton(onClick = onKeywordsClick, text = "Keywords (${uiState.numberOfKeywords})", modifier = Modifier.weight(1f))
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
                border = BorderStroke(width = Dimens.borderStroke, color = CbRedPrimary),
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

@Preview(name = "Light Theme", showBackground = true)
@Composable
fun BannerTestScreenLightPreview() {
    AdPreviewAppTheme {
        BannerTestContent(
            uiState =
                BannerTestUiState(
                    adTitle = "Some test location",
                    loadingButtonState = LoadingButtonState.ENABLED,
                    isClearBtnEnabled = false,
                    numberOfKeywords = 5,
                    callbackChipMap =
                        mapOf(
                            "onAdLoaded" to CallbackChipState.TRIGGERED,
                            "onAdFailed" to CallbackChipState.NOT_TRIGGERED,
                            "onAdImpression" to CallbackChipState.ERROR,
                        ),
                ),
            onLoadClicked = {},
            onClearClicked = {},
            onKeywordsClicked = {},
            onBackClicked = {},
            onSettingsClicked = {},
            eventLog = listOf("onAdLoaded", "onAdClicked", "loadSucceeded"),
            onClearLogs = {},
            onAndroidViewRelease = {},
        )
    }
}

@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun BannerTestScreenDarkPreview() {
    AdPreviewAppTheme(darkTheme = true) {
        BannerTestContent(
            uiState =
                BannerTestUiState(
                    adTitle = "Some test location",
                    loadingButtonState = LoadingButtonState.ENABLED,
                    isClearBtnEnabled = false,
                    numberOfKeywords = 5,
                    callbackChipMap =
                        mapOf(
                            "onAdLoaded" to CallbackChipState.TRIGGERED,
                            "onAdFailed" to CallbackChipState.NOT_TRIGGERED,
                            "onAdImpression" to CallbackChipState.ERROR,
                        ),
                ),
            onLoadClicked = {},
            onClearClicked = {},
            onKeywordsClicked = {},
            onBackClicked = {},
            onSettingsClicked = {},
            eventLog = listOf("onAdLoaded", "onAdClicked", "loadSucceeded"),
            onClearLogs = {},
            onAndroidViewRelease = {},
        )
    }
}
