/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.domain.usecase

import com.chartboost.adpreviewapp.data.local.AuthCredentialsStore
import javax.inject.Inject

class CheckIfLoggedInUseCase
    @Inject
    constructor(
        private val authCredentialsStore: AuthCredentialsStore,
    ) {
        suspend operator fun invoke(): Boolean {
            val credentials = authCredentialsStore.load() ?: return false
            return credentials.accessToken.isNotBlank() && credentials.expiresAt > System.currentTimeMillis()
        }
    }
