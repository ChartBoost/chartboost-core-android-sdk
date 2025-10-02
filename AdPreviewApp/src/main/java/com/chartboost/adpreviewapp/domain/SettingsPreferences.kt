/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.domain

interface SettingsPreferences {
    suspend fun isTestMode(): Boolean

    suspend fun clearLogsOnLoad(): Boolean

    suspend fun setTestMode(enabled: Boolean)

    suspend fun setClearLogsOnLoad(enabled: Boolean)
}
