/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

/**
 * An environment that contains information intended for analytics purposes.
 */
interface AnalyticsEnvironment : BaseEnvironment {
    /**
     * The current app session duration in seconds.
     */
    val appSessionDurationSeconds: Double

    /**
     * The Chartboost-specified unique app session identifier for this app launch.
     */
    val appSessionIdentifier: String?

    /**
     * The version of the app.
     */
    val appVersion: String?

    /**
     * The wrapper or framework used to create this app. ie. Unity.
     */
    val frameworkName: String?

    /**
     * The version of the framework used to create this app.
     */
    val frameworkVersion: String?

    /**
     * Whether or not the publisher reported that the current user is underage as per COPPA.
     */
    val isUserUnderage: Boolean?

    /**
     * The type of network that is currently connected, if any.
     */
    val networkConnectionType: NetworkConnectionType

    /**
     * The publisher-specified player identifier. This is typically used for rewarding users in the
     * rewarded ad format and also for user tracking.
     */
    val playerIdentifier: String?

    /**
     * The publisher-specified app identifier. This is usually what a publisher uses to identify
     * their app.
     */
    val publisherAppIdentifier: String?

    /**
     * The publisher-specified session identifier. This is recommended to be a unique string
     * for every app launch.
     */
    val publisherSessionIdentifier: String?

    /**
     * A number [0.0, 1.0] to signify the current volume of the device.
     */
    val volume: Double?

    /**
     * Gets the WebView user agent.
     *
     * @return The user agent.
     */
    suspend fun getUserAgent(): String?

    /**
     * Gets the advertising identifier for vendors.
     *
     * @return The vendor identifier if possible.
     */
    suspend fun getVendorIdentifier(): String?

    /**
     * Gets whether the advertising identifier for vendors is sourced from the developer or from
     * the app.
     *
     * @return A [VendorIdScope] or [VendorIdScope.UNKNOWN] if not specified.
     */
    suspend fun getVendorIdentifierScope(): VendorIdScope
}
