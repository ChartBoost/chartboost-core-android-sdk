/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

import com.chartboost.core.ChartboostCore
import com.chartboost.core.ChartboostCoreInternal

/**
 * Use this class to set metadata around the user, session, and environment. These values are not
 * persisted between app launches.
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
     * Set the optional session identifier.
     *
     * @param publisherSessionIdentifier The session identifier.
     */
    fun setPublisherSessionIdentifier(publisherSessionIdentifier: String?) {
        ChartboostCoreInternal.environment.publisherSessionIdentifier = publisherSessionIdentifier
    }

    /**
     * Set the optional app identifier.
     *
     * @param publisherAppIdentifier The app identifier.
     */
    fun setPublisherAppIdentifier(publisherAppIdentifier: String?) {
        ChartboostCoreInternal.environment.publisherAppIdentifier = publisherAppIdentifier
    }

    /**
     * Set the framework from which this library is used, eg. Unity.
     *
     * @param frameworkName The name of the framework.
     */
    fun setFrameworkName(frameworkName: String?) {
        ChartboostCoreInternal.environment.frameworkName = frameworkName
    }

    /**
     * Set the version of the framework from which this library is used, eg. 2023.1.5.
     */
    fun setFrameworkVersion(frameworkVersion: String?) {
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
