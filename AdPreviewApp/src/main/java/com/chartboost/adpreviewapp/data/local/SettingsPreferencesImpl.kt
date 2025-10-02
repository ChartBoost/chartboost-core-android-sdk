/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.chartboost.adpreviewapp.domain.SettingsPreferences
import com.chartboost.adpreviewapp.util.DispatcherProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsPreferencesImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val dispatcherProvider: DispatcherProvider,
    ) : SettingsPreferences {
        override suspend fun isTestMode(): Boolean =
            withContext(dispatcherProvider.io()) {
                dataStore.data.first()[SettingsPreferencesKeys.TEST_MODE] ?: false
            }

        override suspend fun clearLogsOnLoad(): Boolean =
            withContext(dispatcherProvider.io()) {
                dataStore.data.first()[SettingsPreferencesKeys.CLEAR_LOGS_ON_LOAD] ?: false
            }

        override suspend fun setTestMode(enabled: Boolean): Unit =
            withContext(dispatcherProvider.io()) {
                dataStore.edit { prefs -> prefs[SettingsPreferencesKeys.TEST_MODE] = enabled }
            }

        override suspend fun setClearLogsOnLoad(enabled: Boolean): Unit =
            withContext(dispatcherProvider.io()) {
                dataStore.edit { prefs -> prefs[SettingsPreferencesKeys.CLEAR_LOGS_ON_LOAD] = enabled }
            }
    }
