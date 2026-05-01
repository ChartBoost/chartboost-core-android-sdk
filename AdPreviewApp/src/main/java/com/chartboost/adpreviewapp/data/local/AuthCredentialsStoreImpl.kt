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
import com.chartboost.adpreviewapp.data.model.AuthCredentials
import com.chartboost.adpreviewapp.util.DispatcherProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthCredentialsStoreImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val dispatcherProvider: DispatcherProvider,
    ) : AuthCredentialsStore {
        private var cachedCredentials: AuthCredentials? = null

        override suspend fun save(credentials: AuthCredentials) {
            cachedCredentials = credentials
            withContext(dispatcherProvider.io()) {
                dataStore.edit { prefs ->
                    prefs[AuthPreferencesKeys.ACCESS_TOKEN] = credentials.accessToken
                    prefs[AuthPreferencesKeys.ID_TOKEN] = credentials.idToken
                    credentials.refreshToken?.let {
                        prefs[AuthPreferencesKeys.REFRESH_TOKEN] = it
                    }
                    prefs[AuthPreferencesKeys.EXPIRES_AT] = credentials.expiresAt
                }
            }
        }

        override suspend fun load(): AuthCredentials? {
            if (cachedCredentials != null) return cachedCredentials

            val prefs = dataStore.data.map { it }.first()
            val accessToken = prefs[AuthPreferencesKeys.ACCESS_TOKEN] ?: return null
            val idToken = prefs[AuthPreferencesKeys.ID_TOKEN] ?: return null
            val refreshToken = prefs[AuthPreferencesKeys.REFRESH_TOKEN]
            val expiresAt = prefs[AuthPreferencesKeys.EXPIRES_AT] ?: 0L

            cachedCredentials = AuthCredentials(accessToken, idToken, refreshToken, expiresAt)
            return cachedCredentials
        }

        override suspend fun clear() {
            cachedCredentials = null
            dataStore.edit { it.clear() }
        }

        override fun getCachedToken(): String? = cachedCredentials?.accessToken
    }
