/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.service

sealed interface SystemCredentialFetchResult {
    data class Success(val email: String, val password: String) : SystemCredentialFetchResult

    data object None : SystemCredentialFetchResult

    data object Canceled : SystemCredentialFetchResult

    data class Error(val cause: Throwable) : SystemCredentialFetchResult
}
