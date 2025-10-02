/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.ui.theme.Dimens
import com.chartboost.adpreviewapp.ui.utils.AppTopBar

@Composable
fun SettingsScreen(
    isTestMode: Boolean,
    showLmtStatus: Boolean,
    clearLogsOnLoad: Boolean,
    lmtStatus: LmtStatus,
    mediationSdkVersion: String,
    monetizationSdkVersion: String,
    onBackClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
    onTestModeChecked: (Boolean) -> Unit,
    onClearLogsChecked: (Boolean) -> Unit,
    onLogOutClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.settings_title)) },
                showBackButton = true,
                onBack = onBackClicked,
            )
        },
        content = { paddingValues ->
            ScreenContent(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = Dimens.paddingM),
                isTestMode = isTestMode,
                showLmtStatus = showLmtStatus,
                lmtStatus = lmtStatus,
                mediationSdkVersion = mediationSdkVersion,
                monetizationSdkVersion = monetizationSdkVersion,
                onTestModeChecked = onTestModeChecked,
                onClearLogsChecked = onClearLogsChecked,
                onLogOutClicked = onLogOutClicked,
                clearLogsOnLoad = clearLogsOnLoad,
                onDeleteAccountClicked = onDeleteAccountClicked,
            )
        },
    )
}

@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    isTestMode: Boolean,
    showLmtStatus: Boolean,
    lmtStatus: LmtStatus,
    mediationSdkVersion: String,
    monetizationSdkVersion: String,
    onTestModeChecked: (Boolean) -> Unit,
    onClearLogsChecked: (Boolean) -> Unit,
    onLogOutClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
    clearLogsOnLoad: Boolean,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingM),
    ) {
        SettingToggle(
            title = stringResource(R.string.test_mode_title),
            checked = isTestMode,
            onCheckedChange = onTestModeChecked,
            description = stringResource(R.string.test_mode_description),
        )

        if (showLmtStatus) {
            SettingInfo(
                title = stringResource(R.string.tracking_status_title),
                value = stringResource(lmtStatus.displayRes),
                description = stringResource(R.string.tracking_status_description),
                valueColor =
                    if (lmtStatus == LmtStatus.GRANTED) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
        }

        SettingToggle(
            title = stringResource(R.string.clear_logs_title),
            checked = clearLogsOnLoad,
            onCheckedChange = onClearLogsChecked,
            description = stringResource(R.string.clear_logs_description),
        )

        DeleteAccountRow(
            onDeleteAccountClicked = onDeleteAccountClicked,
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onLogOutClicked,
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = Dimens.paddingM),
        ) {
            Text(stringResource(R.string.logout))
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.paddingM),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.mediation_sdk_version, mediationSdkVersion),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text =
                    stringResource(
                        R.string.monetization_sdk_version,
                        monetizationSdkVersion,
                    ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DeleteAccountRow(
    onDeleteAccountClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacerXS),
        ) {
            Text(
                text = stringResource(R.string.delete_account),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(R.string.delete_account_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(
            onClick = onDeleteAccountClicked,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_account),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingInfo(
    title: String,
    value: String,
    description: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.paddingS),
    ) {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(Dimens.spacerS))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor)

        description?.let {
            Spacer(modifier = Modifier.height(Dimens.spacerS))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.paddingS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacerXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview
@Composable
private fun PreviewSettingsScreen() {
    SettingsScreen(
        isTestMode = true,
        showLmtStatus = true,
        clearLogsOnLoad = true,
        lmtStatus = LmtStatus.UNKNOWN,
        mediationSdkVersion = "1.0.0",
        monetizationSdkVersion = "1.0.0",
        onBackClicked = {},
        onTestModeChecked = {},
        onClearLogsChecked = {},
        onLogOutClicked = {},
        onDeleteAccountClicked = {},
    )
}
