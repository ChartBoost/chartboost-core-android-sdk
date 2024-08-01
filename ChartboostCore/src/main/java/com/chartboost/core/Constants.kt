/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

/**
 * Constants for Chartboost Core.
 */
internal object Constants {
    /**
     * The Chartboost Core SDK version.
     */
    const val SDK_VERSION: String = BuildConfig.CHARTBOOST_CORE_VERSION

    /**
     * The maximum number of attempts for retrying an action with exponential backoff.
     * TODO: Make this server configurable.
     */
    const val MAX_RETRY_ATTEMPTS: Int = 3

    /**
     * The maximum delay between attempts for retrying an action with exponential backoff.
     * TODO: Make this server configurable.
     */
    const val MAX_RETRY_DELAY_MS: Long = 30000L

    /**
     * The seed delay for retrying an action with exponential backoff.
     * TODO: Make this server configurable.
     */
    const val SEED_RETRY_DELAY_MS: Long = 1000L
}
