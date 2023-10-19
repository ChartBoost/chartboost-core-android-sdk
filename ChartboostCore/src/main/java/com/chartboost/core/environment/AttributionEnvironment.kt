/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

interface AttributionEnvironment {
    suspend fun getAdvertisingIdentifier(): String?
    suspend fun getUserAgent(): String?
}
