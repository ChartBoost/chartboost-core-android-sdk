/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.local

import com.chartboost.adpreviewapp.data.model.AuthCredentials

interface AuthCredentialsStore {
    suspend fun save(credentials: AuthCredentials)

    suspend fun load(): AuthCredentials?

    suspend fun clear()

    fun getCachedToken(): String?
}
