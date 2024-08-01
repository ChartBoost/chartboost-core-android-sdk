/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.canary

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import com.chartboost.core.ChartboostCore
import com.chartboost.core.canary.Constants.CHARTBOOST_GREEN_COLOR
import com.chartboost.core.canary.Constants.CHARTBOOST_RED_COLOR
import com.chartboost.core.initialization.ModuleInitializationResult
import com.chartboost.core.initialization.ModuleObserver
import com.chartboost.core.initialization.SdkConfiguration

/**
 * The controller for this app to interact with the Chartboost Core SDK and its modules.
 */
object CoreController {
    /**
     * Initialize the Chartboost Core SDK with sample modules and data.
     *
     * @param context The [Context] to initialize the SDK with.
     * @param sdkConfiguration The [SdkConfiguration] to initialize the SDK with.
     * @param iconTintMap The map of icon tint colors to update when the module is initialized.
     * @param logViewModel The [LogViewModel] to log initialization events to.
     */
    suspend fun initialize(
        context: Context,
        sdkConfiguration: SdkConfiguration,
        iconTintMap: Map<String, MutableState<Color>>,
        logViewModel: LogViewModel,
    ) {
        logViewModel.log(
            message = "Core SDK initialization started",
            type = LogType.INFO,
            category = LogCategory.INITIALIZATION,
            details = sdkConfiguration.toString(),
        )

        ChartboostCore.initializeSdk(
            context,
            sdkConfiguration,
            object : ModuleObserver {
                override fun onModuleInitializationCompleted(result: ModuleInitializationResult) {
                    logViewModel.log(
                        message = "Core module ${result.moduleId} initialization completed",
                        type = LogType.INFO,
                        category = LogCategory.INITIALIZATION,
                        details = result.toString(),
                    )
                    iconTintMap[result.moduleId]?.value =
                        if (result.exception == null) {
                            CHARTBOOST_GREEN_COLOR
                        } else {
                            CHARTBOOST_RED_COLOR
                        }
                }
            },
        )

        logViewModel.log(
            message = "Core SDK initialization completed",
            type = LogType.INFO,
            category = LogCategory.INITIALIZATION,
            details = "",
        )
    }
}
