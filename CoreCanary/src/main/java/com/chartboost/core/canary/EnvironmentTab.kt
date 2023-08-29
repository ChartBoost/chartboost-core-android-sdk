/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.canary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chartboost.core.ChartboostCore
import com.chartboost.core.environment.AnalyticsEnvironment
import com.chartboost.core.environment.BaseEnvironment
import com.chartboost.core.environment.PublisherMetadata
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.memberProperties

/**
 * UI for the Environment tab.
 */
class EnvironmentTab {
    companion object {
        private val publisherMetadata = ChartboostCore.publisherMetadata
        private val publisherMetadataProps = PublisherMetadata::class
            .memberProperties
            .sortedBy { it.name }

        private val analyticsEnvironment = ChartboostCore.analyticsEnvironment
        private val analyticsProps = (AnalyticsEnvironment::class
            .memberProperties.map { it.name }.toSet() - publisherMetadataProps.map { it.name }
            .toSet())
            .map { propName ->
                AnalyticsEnvironment::class.memberProperties.first { it.name == propName }
            }
            .sortedBy { it.name }
        private val analyticsMethods =
            AnalyticsEnvironment::class.declaredFunctions.sortedBy { it.name }

        private val advertisingEnvironment = ChartboostCore.advertisingEnvironment
        // BaseEnvironment is where these are defined.
        private val advertisingMethods =
            BaseEnvironment::class.declaredFunctions.sortedBy { it.name }

        /**
         * The main tab content.
         */
        @Composable
        fun TabContent() {
            LazyColumn() {
                item {
                    Text(
                        text = "Readwrite Fields",
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                items(publisherMetadataProps.size) { index ->
                    val property = publisherMetadataProps[index]
                    val fieldName = property.name
                    val fieldValue = property.get(publisherMetadata)?.toString() ?: "null"

                    EnvironmentListItem(
                        fieldName = fieldName,
                        fieldValue = fieldValue,
                    )
                }

                item {
                    Text(
                        text = "Readonly Fields",
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                items(analyticsProps.size) { index ->
                    val property = analyticsProps[index]
                    val fieldName = property.name
                    val fieldValue = property.get(analyticsEnvironment)?.toString() ?: "null"

                    EnvironmentListItem(
                        fieldName = fieldName,
                        fieldValue = fieldValue,
                        isReadOnly = true,
                    )
                }

                items(analyticsMethods.size) { index ->
                    FunctionItemDetails(index, analyticsMethods, analyticsEnvironment)
                }

                items(advertisingMethods.size) { index ->
                    FunctionItemDetails(index, advertisingMethods, advertisingEnvironment)
                }

                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }

        @Composable
        private fun FunctionItemDetails(
            index: Int,
            functions: List<KFunction<*>>,
            instance: Any
        ) {
            val function = functions[index]
            val fieldName = function.name
            var fieldValue by remember { mutableStateOf("null") }
            LaunchedEffect(fieldValue) {
                fieldValue = function.callSuspend(instance)?.toString() ?: "null"
            }

            ListItem(
                headlineContent = { Text(fieldName) },
                supportingContent = { Text(fieldValue) },
                modifier = Modifier.clickable(
                    onClick = {}
                )
            )
            Divider()
        }

        /**
         * A list item that displays an environment field with a value.
         *
         * @param fieldName The name of the field.
         * @param fieldValue The value of the field.
         * @param isReadOnly Whether the field is read-only.
         */
        @Composable
        fun EnvironmentListItem(
            fieldName: String,
            fieldValue: String,
            isReadOnly: Boolean = false,
        ) {
            val fieldValueState = remember { mutableStateOf(fieldValue) }
            val showDialog = remember { mutableStateOf(false) }

            ListItem(
                headlineContent = { Text(fieldName) },
                supportingContent = { Text(fieldValueState.value) },
                modifier = Modifier.clickable(
                    onClick = {
                        if (!isReadOnly) {
                            showDialog.value = true
                        }
                    }
                )
            )
            Divider()

            if (showDialog.value) {
                AlertDialog(
                    showDialog = showDialog,
                    title = "Environment",
                    body = "Set a value for '$fieldName' by typing in the text field below.",
                    fieldName = fieldName,
                    fieldValue = fieldValueState.value,
                    onSave = { newFieldValue ->
                        fieldValueState.value = newFieldValue

                        ChartboostCore.publisherMetadata.javaClass
                            .getDeclaredField(fieldName)
                            .apply {
                                isAccessible = true
                                set(ChartboostCore.publisherMetadata, newFieldValue)
                            }
                    }
                )
            }
        }

        /**
         * Displays an alert dialog for setting a new value for an environment field.
         *
         * @param showDialog Whether to show the dialog.
         * @param title The title of the dialog.
         * @param body The body of the dialog.
         * @param fieldName The name of the field.
         * @param fieldValue The value of the field.
         * @param onSave The callback to call when the user saves the new value.
         */
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun AlertDialog(
            showDialog: MutableState<Boolean>,
            title: String,
            body: String,
            fieldName: String,
            fieldValue: String,
            onSave: (String) -> Unit,
        ) {
            val fieldValueState = remember { mutableStateOf(fieldValue) }

            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                }
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        OutlinedTextField(
                            value = fieldValueState.value,
                            onValueChange = { fieldValueState.value = it },
                            label = { Text(fieldName) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showDialog.value = false
                                },
                            ) {
                                Text("Discard")
                            }

                            TextButton(
                                onClick = {
                                    onSave(fieldValueState.value)
                                    showDialog.value = false
                                },
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }
}
