/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.sdk

import android.content.Context
import android.util.Log
import com.chartboost.adpreviewapp.domain.SettingsPreferences
import com.chartboost.adpreviewapp.domain.sdk.ChartboostSdkInitializer
import com.chartboost.adpreviewapp.domain.sdk.ChartboostSdkTestModeSwitcher
import com.chartboost.chartboostmediationsdk.ChartboostMediationSdk
import com.chartboost.core.ChartboostCore
import com.chartboost.core.initialization.ModuleInitializationResult
import com.chartboost.core.initialization.ModuleObserver
import com.chartboost.core.initialization.SdkConfiguration
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

class ChartboostSdkInitializerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val settingsPreferences: SettingsPreferences,
        private val testModeSwitcher: ChartboostSdkTestModeSwitcher,
    ) : ChartboostSdkInitializer {
        override suspend fun initialize(appId: String): Result<Unit> {
            val testMode = settingsPreferences.isTestMode()
            testModeSwitcher.setTestMode(testMode)
            val config = SdkConfiguration(appId, modules = emptyList())
            val deferredResult = CompletableDeferred<Result<Unit>>()
            ChartboostCore.initializeSdk(
                context = context,
                sdkConfiguration = config,
                observer =
                    object : ModuleObserver {
                        override fun onModuleInitializationCompleted(result: ModuleInitializationResult) {
                            if (result.moduleId == ChartboostMediationSdk.CORE_MODULE_ID) {
                                result.exception?.let {
                                    Log.e(TAG, INITIALIZATION_ERROR_LOG_MESSAGE, it)
                                    deferredResult.complete(Result.failure(it))
                                } ?: deferredResult.complete(Result.success(Unit))
                            }
                        }
                    },
            )

            return deferredResult.await()
        }

        companion object {
            private val TAG = ChartboostSdkInitializerImpl::class.simpleName
            private const val INITIALIZATION_ERROR_LOG_MESSAGE = "Initializing CB mediation SDK failed"
        }
    }
