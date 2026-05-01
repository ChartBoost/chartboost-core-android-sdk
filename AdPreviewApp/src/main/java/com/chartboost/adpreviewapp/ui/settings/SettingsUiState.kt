/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.settings

data class SettingsUiState(
    val isTestMode: Boolean = false,
    val clearLogsOnLoad: Boolean = false,
    val lmtStatus: LmtStatus = LmtStatus.UNKNOWN,
    val mediationSdkVersion: String = "unknown",
    val monetizationSdkVersion: String = "unknown",
    val isLoggedOut: Boolean = false,
    val showTestModeErrorMessage: Boolean = false,
) {
    val showLmtStatus: Boolean
        get() = false // re-enable when LmtStatusChecker is implemented
}
