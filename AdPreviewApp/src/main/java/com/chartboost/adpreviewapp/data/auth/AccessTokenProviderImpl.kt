/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.auth

import com.chartboost.adpreviewapp.data.local.AuthCredentialsStore
import javax.inject.Inject

class AccessTokenProviderImpl
    @Inject
    constructor(
        private val credentialsStore: AuthCredentialsStore,
    ) : AccessTokenProvider {
        override fun getToken(): String? {
            return credentialsStore.getCachedToken()
        }
    }
