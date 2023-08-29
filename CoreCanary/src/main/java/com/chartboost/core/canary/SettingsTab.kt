/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.canary

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.chartboost.core.ChartboostCore

/**
 * UI for the Settings tab.
 */
object SettingsTab {
    /**
     * The main tab content.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TabContent() {
        val context = LocalContext.current

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = BottomSheetDefaults.SheetPeekHeight)
        ) {
            item {
                Text(
                    text = "Metadata",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
            item {
                MetadataListItem(fieldName = "Core SDK version", fieldValue = ChartboostCore.sdkVersion)
                MetadataListItem(fieldName = "Canary version", fieldValue = "1.0.0")
                MetadataListItem(
                    fieldName = "Core release timestamp",
                    fieldValue = "2023-09-07 18:00:00.000"
                )
                MetadataListItem(
                    fieldName = "Canary release timestamp",
                    fieldValue = "2023-09-07 18:00:00.000"
                )
            }
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
            item {
                SettingsListItem(
                    settingName = "Debug Mode",
                    settingKey = "debugModeEnabled",
                    context = context,
                    onSettingChange = { newValue ->
                        ChartboostCore.debug = newValue
                    }
                )
            }
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
            item {
                Text(
                    text = "This is a sample app developed and maintained by the Chartboost team to " +
                            "interact with the Chartboost Core SDK and its underlying modules. " +
                            "Usage of this app is subject to the Chartboost Terms of Service and " +
                            "Privacy Policy. This app is not intended for production use. " +
                            "For more information, please visit https://chartboost.com.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    /**
     * A list item for a metadata field.
     *
     * @param fieldName The name of the metadata field.
     * @param fieldValue The value of the metadata field.
     */
    @Composable
    fun MetadataListItem(
        fieldName: String,
        fieldValue: String,
    ) {
        val fieldValueState = remember { mutableStateOf(fieldValue) }

        ListItem(
            headlineContent = { Text(fieldName) },
            supportingContent = { Text(fieldValueState.value) },
        )
        Divider()
    }

    /**
     * A list item that displays a setting name, and a toggleable switch to change the setting.
     * This is a generic implementation that can be used for any setting that is binary. Don't
     * hard-code any setting-specific logic here. Instead, pass in the setting name, key, and
     * handle the setting change in the onSettingChange callback.
     *
     * @param settingName The name of the setting to display.
     * @param settingKey The key of that setting in SharedPreferences.
     * @param context The context to use to access SharedPreferences.
     * @param onSettingChange A callback to handle the setting change.
     */
    @Composable
    fun SettingsListItem(
        settingName: String,
        settingKey: String,
        context: Context,
        onSettingChange: (Boolean) -> Unit,
    ) {
        val sharedPref = context.getSharedPreferences(
            "com.chartboost.core.canary", Context.MODE_PRIVATE
        )
        var toggleState by remember {
            mutableStateOf(sharedPref.getBoolean(settingKey, true))
        }

        ListItem(
            headlineContent = { Text(settingName) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                )
            },
            trailingContent = {
                Row(
                    Modifier
                        .toggleable(
                            role = Role.Switch,
                            value = toggleState,
                            onValueChange = { newValue ->
                                toggleState = newValue
                                sharedPref.edit().putBoolean(settingKey, newValue).apply()
                                onSettingChange(newValue)
                            },
                        )
                ) {
                    Switch(
                        checked = toggleState,
                        onCheckedChange = null,
                    )
                }
            }
        )
        Divider()
    }
}
