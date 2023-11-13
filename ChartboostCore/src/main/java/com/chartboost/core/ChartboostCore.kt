/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import android.content.Context
import com.chartboost.core.ChartboostCoreInternal.environment
import com.chartboost.core.consent.ConsentManagementPlatform
import com.chartboost.core.environment.*
import com.chartboost.core.initialization.InitializableModule
import com.chartboost.core.initialization.InitializableModuleObserver
import com.chartboost.core.initialization.SdkConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
     * Flag to enable debug mode for Chartboost Core. This will enable verbose logs for the SDK.
     */
    @JvmStatic
    var debug: Boolean = false

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
     * Initialize the Chartboost Core SDK and its modules for Java implementations.
     *
     * @param context Context to use for initialization.
     * @param sdkConfiguration ChartboostCore configuration to use for initialization.
     * @param modules Set of modules to initialize.
     * @param observer Observer for module initialization to be notified of each module's result data.
     */
    @JvmStatic
    fun initializeSdkFromJava(
        context: Context,
        sdkConfiguration: SdkConfiguration,
        modules: List<InitializableModule>,
        observer: InitializableModuleObserver?,
    ) {
        CoroutineScope(Main).launch {
            initializeSdk(
                context,
                sdkConfiguration,
                modules,
                observer
            )
        }
    }

    /**
     * Initialize the Chartboost Core SDK and its modules.
     *
     * @param context Context to use for initialization.
     * @param sdkConfiguration ChartboostCore configuration to use for initialization.
     * @param modules Set of modules to initialize.
     * @param observer Observer for module initialization to be notified of each module's result data.
     *
     * @return Result data pertaining to the Chartboost Core SDK initialization itself.
     */
    suspend fun initializeSdk(
        context: Context,
        sdkConfiguration: SdkConfiguration,
        modules: List<InitializableModule>,
        observer: InitializableModuleObserver?,
    ) = coroutineScope {
        ChartboostCoreInternal.initializeSdk(context, sdkConfiguration, modules, observer)
    }
}
