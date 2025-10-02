/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.data.model.App
import com.chartboost.adpreviewapp.ui.theme.Dimens
import com.chartboost.adpreviewapp.ui.utils.AppTopBar
import com.chartboost.adpreviewapp.ui.utils.SearchBar
import com.chartboost.adpreviewapp.ui.utils.ShowToastEffect

@Composable
fun AppsScreen(
    onSettingsClicked: () -> Unit,
    onAppSelected: (String) -> Unit,
    viewModel: AppsListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val result = uiState.sdkInitResult) {
        is SdkInitResult.Success -> {
            onAppSelected(result.appId)
            viewModel.onSdkInitHandled()
            ShowToastEffect(
                message = "SDK initialized for: ${result.appId}",
                onToastShown = viewModel::onSdkInitHandled,
            )
        }

        is SdkInitResult.Failure -> {
            ShowToastEffect(
                message = "Init failed: ${result.issue}",
                onToastShown = viewModel::onSdkInitHandled,
            )
        }

        else -> Unit
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.apps_screen_title)) },
                showBackButton = false,
                showSettingsButton = true,
                onSettingsClick = onSettingsClicked,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Dimens.paddingM),
        ) {
            SearchBar(
                label = stringResource(R.string.search_apps_label),
                query = uiState.query,
                onQueryChanged = viewModel::onQueryChanged,
            )

            Spacer(modifier = Modifier.height(Dimens.spacerM))

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Text(
                        text = stringResource(R.string.error_prefix, uiState.error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                uiState.filteredApps.isEmpty() -> {
                    Text(stringResource(R.string.no_apps_found))
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn {
                            items(uiState.filteredApps) { app ->
                                val isItemEnabled = !uiState.isLoading && uiState.sdkInitResult == null
                                AppListItem(app = app, enabled = isItemEnabled) {
                                    viewModel.onAppSelected(app.id)
                                }
                            }
                        }

                        // overlay loading indicator while SDK is initializing
                        if (uiState.isSdkInitializing) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    app: App,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = Dimens.paddingS),
        tonalElevation = Dimens.elevationSmall,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubcomposeAsyncImage(
                model = app.icon.takeIf { !it.isNullOrBlank() },
                contentDescription = "${app.nickname ?: app.name} icon",
                modifier =
                    Modifier
                        .size(Dimens.paddingXXL)
                        .padding(end = Dimens.paddingM),
            ) {
                when (painter.state) {
                    is coil.compose.AsyncImagePainter.State.Success -> {
                        SubcomposeAsyncImageContent()
                    }
                    is coil.compose.AsyncImagePainter.State.Error,
                    is coil.compose.AsyncImagePainter.State.Empty,
                    -> {
                        Icon(
                            imageVector = Icons.Default.ImageNotSupported,
                            contentDescription = "Default app icon",
                            modifier = Modifier.size(Dimens.iconSizeM),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> Unit
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                val nameOrFallback = app.nickname?.ifEmpty { app.name } ?: stringResource(R.string.unnamed_app, app.id)

                Text(
                    text = nameOrFallback,
                    style = MaterialTheme.typography.titleMedium,
                )

                app.platform?.let {
                    Text(stringResource(R.string.platform_label, it), style = MaterialTheme.typography.bodySmall)
                }

                app.storeAppId?.let {
                    Text(stringResource(R.string.bundle_id_label, it), style = MaterialTheme.typography.bodySmall)
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.view_placements_description),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppListItemPreview() {
    AppListItem(
        app =
            App(
                id = "abc123",
                name = "MyTestApp",
                nickname = "TestApp",
                platform = "android",
                storeAppId = "com.test.app",
                icon = "https://developer.android.com/images/brand/Android_Robot.png",
            ),
        onClick = {},
    )
}
