/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.sdk

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.chartboost.adpreviewapp.domain.sdk.ChartboostSdkTestModeSwitcher
import com.chartboost.chartboostmediationsdk.ChartboostMediationSdk
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ChartboostSdkTestModeSwitcherImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ChartboostSdkTestModeSwitcher {
        override fun setTestMode(testMode: Boolean): Result<Unit> =
            if (isDebuggable(context)) {
                ChartboostMediationSdk.setTestMode(context, testMode)
                Result.success(Unit)
            } else {
                setTestModeReflectively(testMode)
            }

        private fun setTestModeReflectively(testMode: Boolean): Result<Unit> =
            try {
                Log.d(TAG, "Setting testMode using reflection")
                val cbMediationClass = ChartboostMediationSdk::class.java
                val cbMediationInternalField = cbMediationClass.getDeclaredField("chartboostMediationInternal")
                cbMediationInternalField.isAccessible = true

                val cbMediationInternalInstance = cbMediationInternalField.get(null) // null for static field
                val testModeField = cbMediationInternalInstance.javaClass.getDeclaredField("testMode")
                testModeField.isAccessible = true
                testModeField.set(cbMediationInternalInstance, testMode)

                Result.success(Unit)
            } catch (e: SecurityException) {
                logReflectionError(e)
                Result.failure(e)
            } catch (e: NoSuchFieldException) {
                logReflectionError(e)
                Result.failure(e)
            } catch (e: IllegalAccessException) {
                logReflectionError(e)
                Result.failure(e)
            } catch (e: IllegalArgumentException) {
                logReflectionError(e)
                Result.failure(e)
            }

        /**
         * Check if debuggable.
         * Copied from ChartboostMeditationSdk
         */
        private fun isDebuggable(context: Context): Boolean = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        private fun logReflectionError(throwable: Throwable) {
            val messageCore = "Can't set testMode using reflection - "
            val suffix =
                when (throwable) {
                    is SecurityException -> "SecurityException: ${throwable.message}"
                    is NoSuchFieldException -> "NoSuchFieldException: ${throwable.message}"
                    is IllegalStateException -> "IllegalStateException: ${throwable.message}"
                    is IllegalArgumentException -> "IllegalArgumentException: ${throwable.message}"
                    else -> throwable.message.orEmpty()
                }
            Log.e(TAG, messageCore.plus(suffix), throwable)
        }

        companion object {
            private val TAG = ChartboostSdkTestModeSwitcherImpl::class.java.simpleName
        }
    }
