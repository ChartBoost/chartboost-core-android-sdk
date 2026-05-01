/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.keywords

data class KeywordsUiState(
    val keywords: Map<String, String> = emptyMap(),
    val isAddKeywordDialogVisible: Boolean = false,
    val isDeleteAllDialogVisible: Boolean = false,
    val keyInput: String = "",
    val valueInput: String = "",
)
