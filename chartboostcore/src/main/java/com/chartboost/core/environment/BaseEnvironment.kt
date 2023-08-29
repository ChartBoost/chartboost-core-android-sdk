/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

interface BaseEnvironment {
    val bundleIdentifier: String?
    val deviceLocale: String?
    val deviceMake: String
    val deviceModel: String
    val osName: String
    val osVersion: String
    val screenHeight: Int?
    val screenScale: Float?
    val screenWidth: Int?

    suspend fun getAdvertisingIdentifier(): String?
    suspend fun getLimitAdTrackingEnabled(): Boolean?
}
