/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.keywords

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.ui.theme.Dimens
import com.chartboost.adpreviewapp.ui.utils.AppTopBar

@Composable
fun KeywordsScreen(
    onBackClicked: () -> Unit,
    viewModel: KeywordsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.keywords_title)) },
                showBackButton = true,
                onBack = onBackClicked,
                showDeleteAllButton = uiState.keywords.isNotEmpty(),
                onDeleteAllClick = { viewModel.onDeleteAllClicked() },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.buttonClicked() },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Keyword")
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            if (uiState.keywords.isEmpty()) {
                Text(
                    text = "No keywords added yet",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(Dimens.paddingM),
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingS),
                ) {
                    items(uiState.keywords.toList()) { (key, value) ->
                        KeywordItem(key = key, value = value)
                    }
                }
            }
        }
    }

    if (uiState.isAddKeywordDialogVisible) {
        AddKeywordDialog(
            keyInput = uiState.keyInput,
            valueInput = uiState.valueInput,
            onKeyChanged = viewModel::onKeyChanged,
            onValueChanged = viewModel::onValueChanged,
            onSave = viewModel::onSaveKeyword,
            onDismiss = viewModel::onDialogDismiss,
        )
    }

    if (uiState.isDeleteAllDialogVisible) {
        DeleteAllConfirmationDialog(
            onConfirm = viewModel::onDeleteAllConfirmed,
            onDismiss = viewModel::onDeleteAllDialogDismiss,
        )
    }
}

@Composable
private fun KeywordItem(
    key: String,
    value: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingM),
        ) {
            Text(
                text = "Key: $key",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Value: $value",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AddKeywordDialog(
    keyInput: String,
    valueInput: String,
    onKeyChanged: (String) -> Unit,
    onValueChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Keyword") },
        text = {
            Column {
                Text("Key")
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = onKeyChanged,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Value", modifier = Modifier.padding(top = Dimens.paddingS))
                OutlinedTextField(
                    value = valueInput,
                    onValueChange = onValueChanged,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = keyInput.isNotBlank() && valueInput.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DeleteAllConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete All Keywords") },
        text = { Text("Are you sure you want to delete all keywords? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete All")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
