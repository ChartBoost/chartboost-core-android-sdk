/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

import com.chartboost.core.ChartboostCoreInternal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Use this class to set metadata around the user, session, and environment. These values are not
 * persisted between app launches.
 */
class PublisherMetadata() {
    /**
     * The list of observers to notify when a property changes.
     */
    private val observers = mutableListOf<PublisherMetadataObserver>()

    /**
     * Set if this user is underage for COPPA.
     *
     * @param userUnderage True if the user is underage.
     */
    fun setIsUserUnderage(userUnderage: Boolean) {
        ChartboostCoreInternal.environment.isUserUnderage = userUnderage
        notifyObservers(PublisherMetadataChangedProperty.IS_USER_UNDERAGE)
    }

    /**
     * Set the optional session identifier.
     *
     * @param publisherSessionIdentifier The session identifier.
     */
    fun setPublisherSessionIdentifier(publisherSessionIdentifier: String?) {
        ChartboostCoreInternal.environment.publisherSessionIdentifier = publisherSessionIdentifier
        notifyObservers(PublisherMetadataChangedProperty.PUBLISHER_SESSION_IDENTIFIER)
    }

    /**
     * Set the optional app identifier.
     *
     * @param publisherAppIdentifier The app identifier.
     */
    fun setPublisherAppIdentifier(publisherAppIdentifier: String?) {
        ChartboostCoreInternal.environment.publisherAppIdentifier = publisherAppIdentifier
        notifyObservers(PublisherMetadataChangedProperty.PUBLISHER_APP_IDENTIFIER)
    }

    /**
     * Set the framework from which this library is used, eg. Unity.
     *
     * @param frameworkName The name of the framework.
     */
    fun setFrameworkName(frameworkName: String?) {
        ChartboostCoreInternal.environment.frameworkName = frameworkName
        notifyObservers(PublisherMetadataChangedProperty.FRAMEWORK_NAME)
    }

    /**
     * Set the version of the framework from which this library is used, eg. 2023.1.5.
     */
    fun setFrameworkVersion(frameworkVersion: String?) {
        ChartboostCoreInternal.environment.frameworkVersion = frameworkVersion
        notifyObservers(PublisherMetadataChangedProperty.FRAMEWORK_VERSION)
    }

    /**
     * Set the optional player identifier.
     *
     * @param playerIdentifier The player identifier.
     */
    fun setPlayerIdentifier(playerIdentifier: String?) {
        ChartboostCoreInternal.environment.playerIdentifier = playerIdentifier
        notifyObservers(PublisherMetadataChangedProperty.PLAYER_IDENTIFIER)
    }

    /**
     * Add an observer to be notified when a property changes.
     */
    fun addObserver(observer: PublisherMetadataObserver) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            observers.add(observer)
        }
    }

    /**
     * Remove an observer from being notified when a property changes.
     */
    fun removeObserver(observer: PublisherMetadataObserver) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            observers.remove(observer)
        }
    }

    /**
     * Notify all observers that a property has changed.
     *
     * @param property The property that changed.
     */
    private fun notifyObservers(property: PublisherMetadataChangedProperty) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            observers.forEach {
                it.onChanged(property)
            }
        }
    }
}

/**
 * Type-safe representation of the changed property. This is used to avoid passing strings around.
 */
enum class PublisherMetadataChangedProperty(val value: String) {
    IS_USER_UNDERAGE("isUserUnderage"),
    PUBLISHER_SESSION_IDENTIFIER("publisherSessionID"),
    PUBLISHER_APP_IDENTIFIER("publisherAppID"),
    FRAMEWORK_NAME("frameworkName"),
    FRAMEWORK_VERSION("frameworkVersion"),
    PLAYER_IDENTIFIER("playerID")
}
