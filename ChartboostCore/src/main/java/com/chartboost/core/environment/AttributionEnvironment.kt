/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

/**
 * An environment that contains information intended for attribution purposes.
 */
interface AttributionEnvironment {
    /**
     * Gets the advertising identifier.
     *
     * @return The advertising identifier if available.
     */
    suspend fun getAdvertisingIdentifier(): String?

    /**
     * Gets the WebView user agent.
     *
     * @return The user agent.
     */
    suspend fun getUserAgent(): String?
}
