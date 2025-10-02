/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.locations

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.data.model.AdLocation
import com.chartboost.adpreviewapp.data.model.AdLocationsErrorMessage
import com.chartboost.adpreviewapp.ui.theme.Dimens
import com.chartboost.adpreviewapp.ui.utils.AppTopBar
import com.chartboost.adpreviewapp.ui.utils.ConfirmationDialog
import com.chartboost.adpreviewapp.ui.utils.SearchBar
import com.chartboost.adpreviewapp.util.restartApp

@Composable
fun AdLocationsScreen(
    onLocationClicked: (locationName: String, adType: String) -> Unit,
    onSettingsClicked: () -> Unit,
    viewModel: AdLocationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    BackHandler(enabled = true) {
        viewModel.onBackRequested()
    }

    if (uiState.showForceCloseDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.dialog_restart_title),
            message = stringResource(R.string.dialog_restart_message),
            confirmButtonText = stringResource(R.string.dialog_force_close_confirm),
            dismissButtonText = stringResource(R.string.dialog_restart_dismiss),
            onConfirm = { restartApp(context) },
            onDismiss = { viewModel.dismissForceCloseDialog() },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SubcomposeAsyncImage(
                            model = uiState.iconUrl.takeIf { !it.isNullOrBlank() },
                            contentDescription = "${uiState.nameToDisplay} app icon",
                            modifier =
                                Modifier
                                    .size(Dimens.iconSizeM)
                                    .padding(end = Dimens.paddingM),
                        ) {
                            when (painter.state) {
                                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                                else ->
                                    Icon(
                                        imageVector = Icons.Default.ImageNotSupported,
                                        contentDescription = "Placeholder app icon",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(Dimens.iconSizeM),
                                    )
                            }
                        }
                        Text(
                            text = uiState.nameToDisplay.ifBlank { stringResource(R.string.ad_locations_default_title) },
                            modifier =
                                Modifier
                                    .padding(end = Dimens.paddingS)
                                    .weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                showBackButton = true,
                onBack = viewModel::onBackRequested,
                showSettingsButton = true,
                onSettingsClick = onSettingsClicked,
            )
        },
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Top,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Dimens.paddingM),
        ) {
            SearchBar(
                label = stringResource(id = R.string.search_ad_locations_label),
                query = uiState.query,
                onQueryChanged = viewModel::onQueryChanged,
            )

            Spacer(modifier = Modifier.height(Dimens.spacerM))

            if (uiState.errorMessage == null) {
                AdLocationList(
                    locations = uiState.filteredLocations,
                    onLocationClick = onLocationClicked,
                )
            } else {
                val errorMessage =
                    when (uiState.errorMessage) {
                        AdLocationsErrorMessage.NO_LOCATIONS -> stringResource(R.string.no_locations_found)
                        AdLocationsErrorMessage.ERROR_FETCHING_LOCATIONS -> stringResource(R.string.problem_fetching_locations)
                        AdLocationsErrorMessage.NO_APP_ID -> stringResource(id = R.string.no_app_id_found)
                        null -> ""
                    }
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(Dimens.paddingM),
                )
            }
        }
    }
}

@Composable
private fun AdLocationList(
    locations: List<AdLocation>,
    onLocationClick: (locationName: String, adType: String) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        items(locations) { location ->
            AdLocationListItem(
                location = location,
                onClick = onLocationClick,
            )
        }
    }
}

@Composable
private fun AdLocationListItem(
    location: AdLocation,
    onClick: (locationName: String, adType: String) -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick(location.name, location.adType) }
                .padding(vertical = Dimens.paddingS),
        tonalElevation = Dimens.elevationSmall,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            Modifier
                .padding(Dimens.paddingM),
        ) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )

            Row {
                Text(
                    text = location.adType,
                    modifier = Modifier.padding(end = Dimens.paddingS),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = location.type,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
