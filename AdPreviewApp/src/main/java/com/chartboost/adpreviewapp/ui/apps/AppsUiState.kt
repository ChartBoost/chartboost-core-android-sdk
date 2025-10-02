/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.apps

import com.chartboost.adpreviewapp.data.model.App

data class AppsUiState(
    val isLoading: Boolean = false,
    val apps: List<App> = emptyList(),
    val filteredApps: List<App> = emptyList(),
    val query: String = "",
    val error: String? = null,
    val sdkInitResult: SdkInitResult? = null,
    val isSdkInitializing: Boolean = false,
)

sealed class SdkInitResult {
    data class Success(val appId: String) : SdkInitResult()

    data class Failure(val issue: String) : SdkInitResult()
}
