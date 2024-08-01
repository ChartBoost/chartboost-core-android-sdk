/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import android.content.Context
import com.chartboost.core.ChartboostCoreInternal.environment
import com.chartboost.core.consent.ConsentManagementPlatform
import com.chartboost.core.environment.AdvertisingEnvironment
import com.chartboost.core.environment.AnalyticsEnvironment
import com.chartboost.core.environment.AttributionEnvironment
import com.chartboost.core.environment.PublisherMetadata
import com.chartboost.core.initialization.ModuleFactory
import com.chartboost.core.initialization.ModuleObserver
import com.chartboost.core.initialization.SdkConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * The entry point for ChartboostCore.
 */
object ChartboostCore {
    /**
     * The Chartboost Core Consent Management Platform.
     */
    @JvmStatic
    val consent: ConsentManagementPlatform
        get() = ChartboostCoreInternal.consent

    /**
     * The version of Chartboost Core
     */
    @JvmStatic
    val sdkVersion: String = Constants.SDK_VERSION

    /**
     * The log level. Anything of that log level and lower will be emitted.
     * Set this to [ChartboostCoreLogLevel.DISABLED] for no logs
     */
    @JvmStatic
    var logLevel
        @JvmStatic
        get() = ChartboostCoreLogger.logLevel

        @JvmStatic
        set(value) {
            ChartboostCoreLogger.logLevel = value
        }

    /**
     * Set metadata about the user or app with this class.
     */
    @JvmStatic
    val publisherMetadata: PublisherMetadata = PublisherMetadata()

    /**
     * The environment that contains information intended solely for analytics purposes.
     */
    @JvmStatic
    val analyticsEnvironment: AnalyticsEnvironment
        get() = environment

    /**
     * The environment that contains information intended solely for advertising purposes.
     */
    @JvmStatic
    val advertisingEnvironment: AdvertisingEnvironment
        get() = environment

    /**
     * The environment that contains information intended solely for attribution purposes.
     */
    @JvmStatic
    val attributionEnvironment: AttributionEnvironment
        get() = environment

    /**
     * The mechanism for non-native ChartboostCore Modules to be created by native ChartboostCore.
     */
    @JvmStatic
    internal var nonNativeModuleFactory: ModuleFactory? = null

    /**
     * Initialize the Chartboost Core SDK and its modules for Java implementations.
     *
     * @param context Context to use for initialization.
     * @param sdkConfiguration ChartboostCore configuration to use for initialization.
     * @param observer Observer for module initialization to be notified of each module's result data.
     */
    @JvmStatic
    fun initializeSdkFromJava(
        context: Context,
        sdkConfiguration: SdkConfiguration,
        observer: ModuleObserver?,
    ) {
        CoroutineScope(Main).launch {
            initializeSdk(
                context,
                sdkConfiguration,
                observer,
            )
        }
    }

    /**
     * Initialize the Chartboost Core SDK and its modules.
     *
     * @param context Context to use for initialization.
     * @param sdkConfiguration ChartboostCore configuration to use for initialization.
     * @param observer Observer for module initialization to be notified of each module's result data.
     *
     * @return Result data pertaining to the Chartboost Core SDK initialization itself.
     */
    suspend fun initializeSdk(
        context: Context,
        sdkConfiguration: SdkConfiguration,
        observer: ModuleObserver?,
    ) = coroutineScope {
        ChartboostCoreInternal.initializeSdk(context, sdkConfiguration, observer)
    }
}
