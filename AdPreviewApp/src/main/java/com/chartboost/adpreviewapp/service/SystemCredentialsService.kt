/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.service

/**
 * @class CredentialsService provides ability to utilize an android framework password manager API.
 */
interface SystemCredentialsService {
    suspend fun fetchSystemSavedPassword(preferImmediatelyAvailable: Boolean = false): SystemCredentialFetchResult
}
