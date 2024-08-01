/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

import com.chartboost.core.ChartboostCoreInternal

/**
 * Use this class to set environment properties regarding the user, session, and framework.
 * These values are not persisted between app launches.
 */
class PublisherMetadata() {
    /**
     * Set if this user is underage for COPPA.
     *
     * @param userUnderage True if the user is underage.
     */
    fun setIsUserUnderage(userUnderage: Boolean) {
        ChartboostCoreInternal.environment.isUserUnderage = userUnderage
    }

    /**
     * Set the optional publisher-specified session identifier.
     *
     * @param publisherSessionIdentifier The session identifier.
     */
    fun setPublisherSessionIdentifier(publisherSessionIdentifier: String?) {
        ChartboostCoreInternal.environment.publisherSessionIdentifier = publisherSessionIdentifier
    }

    /**
     * Set the optional publisher-specified app identifier.
     *
     * @param publisherAppIdentifier The app identifier.
     */
    fun setPublisherAppIdentifier(publisherAppIdentifier: String?) {
        ChartboostCoreInternal.environment.publisherAppIdentifier = publisherAppIdentifier
    }

    /**
     * Set the framework name and version from which this library is used, eg. Unity.
     *
     * @param frameworkName The name of the framework.
     * @param frameworkVersion The version of the framework.
     */
    fun setFramework(
        frameworkName: String?,
        frameworkVersion: String?,
    ) {
        ChartboostCoreInternal.environment.frameworkName = frameworkName
        ChartboostCoreInternal.environment.frameworkVersion = frameworkVersion
    }

    /**
     * Set the optional player identifier.
     *
     * @param playerIdentifier The player identifier.
     */
    fun setPlayerIdentifier(playerIdentifier: String?) {
        ChartboostCoreInternal.environment.playerIdentifier = playerIdentifier
    }
}

/**
 * Type-safe representation of the changed property. This is used to avoid passing strings around.
 */
enum class ObservableEnvironmentProperty(val value: String) {
    IS_USER_UNDERAGE("isUserUnderage"),
    PUBLISHER_SESSION_IDENTIFIER("publisherSessionIdentifier"),
    PUBLISHER_APP_IDENTIFIER("publisherAppIdentifier"),
    FRAMEWORK_NAME("frameworkName"),
    FRAMEWORK_VERSION("frameworkVersion"),
    PLAYER_IDENTIFIER("playerIdentifier"),
}
