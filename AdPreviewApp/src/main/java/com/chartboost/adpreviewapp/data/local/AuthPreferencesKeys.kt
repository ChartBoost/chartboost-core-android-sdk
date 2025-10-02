/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.local

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object AuthPreferencesKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val ID_TOKEN = stringPreferencesKey("id_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val EXPIRES_AT = longPreferencesKey("expires_at")
}
