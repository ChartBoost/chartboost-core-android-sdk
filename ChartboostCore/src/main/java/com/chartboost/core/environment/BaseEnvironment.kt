/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

/**
 * Has much of the common environment getters.
 */
interface BaseEnvironment {
    /**
     * The bundle identifier of this application.
     */
    val bundleIdentifier: String?

    /**
     * The device locale.
     */
    val deviceLocale: String?

    /**
     * The make or manufacturer of the device.
     */
    val deviceMake: String

    /**
     * The model of the device. This is usually a consumer product name.
     */
    val deviceModel: String

    /**
     * The operating system name. Always "Android".
     */
    val osName: String

    /**
     * The operating system version.
     */
    val osVersion: String

    /**
     * The height of the screen in pixels.
     */
    val screenHeightPixels: Int?

    /**
     * The logical density of the display. Used to convert pixels to density-independent pixels. See
     * [density](https://developer.android.com/reference/android/util/DisplayMetrics#density)
     * for more information.
     */
    val screenScale: Float?

    /**
     * The width of the screen in pixels.
     */
    val screenWidthPixels: Int?

    /**
     * Gets the advertising identifier.
     *
     * @return The advertising identifier if available.
     */
    suspend fun getAdvertisingIdentifier(): String?

    /**
     * Gets whether or not limit ad tracking is enabled.
     *
     * @return true if LAT is enabled and false otherwise.
     */
    suspend fun getLimitAdTrackingEnabled(): Boolean?

    /**
     * Add an observer to listen for changes of [ObservableEnvironmentProperty].
     */
    fun addObserver(observer: EnvironmentObserver)

    /**
     * Remove an EnvironmentObserver.
     */
    fun removeObserver(observer: EnvironmentObserver)
}
