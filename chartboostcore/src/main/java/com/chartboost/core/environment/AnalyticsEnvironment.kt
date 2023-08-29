/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

interface AnalyticsEnvironment : BaseEnvironment {
    val appSessionDurationSeconds: Double
    val appSessionIdentifier: String?
    val appVersion: String?
    val frameworkName: String?
    val frameworkVersion: String?
    val isUserUnderage: Boolean?
    val networkConnectionType: NetworkConnectionType
    val playerIdentifier: String?
    val publisherAppIdentifier: String?
    val publisherSessionIdentifier: String?
    val volume: Double?

    suspend fun getUserAgent(): String?
    suspend fun getVendorIdentifier(): String?
    suspend fun getVendorIdentifierScope(): VendorIdScope
}
