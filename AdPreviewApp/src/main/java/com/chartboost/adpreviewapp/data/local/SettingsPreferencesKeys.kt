/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey

object SettingsPreferencesKeys {
    val TEST_MODE = booleanPreferencesKey("test_mode")
    val CLEAR_LOGS_ON_LOAD = booleanPreferencesKey("clear_logs_on_load")
}
