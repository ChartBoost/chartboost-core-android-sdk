/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.canary

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.chartboost.core.canary.Constants.CHARTBOOST_GREEN_COLOR
import com.chartboost.core.consent.usercentrics.UsercentricsAdapter
import com.chartboost.core.initialization.InitializableModule
import com.chartboost.core.initialization.InitializableModuleObserver
import com.chartboost.core.initialization.ModuleInitializationResult
import com.chartboost.core.initialization.SdkConfiguration

/**
 * The controller for this app to interact with the Chartboost Core SDK and its modules.
 */
object CoreController {
    /**
     * The logs to display in the app's [BottomSheet].
     */
    val logs = mutableStateOf("")

    /**
     * Initialize the Chartboost Core SDK with sample modules and data.
     *
     * @param context The [Context] to initialize the SDK with.
     * @param sdkConfiguration The [SdkConfiguration] to initialize the SDK with.
     * @param modules The set of modules to initialize.
     * @param iconTintMap The map of icon tint colors to update when the module is initialized.
     */
    suspend fun initialize(
        context: Context,
        sdkConfiguration: SdkConfiguration,
        modules: List<InitializableModule>,
        iconTintMap: Map<String, MutableState<Color>>,
    ) {
        com.chartboost.core.ChartboostCore.initializeSdk(
            context,
            sdkConfiguration,
            modules,
            object : InitializableModuleObserver {
                override fun onModuleInitializationCompleted(result: ModuleInitializationResult) {
                    // TODO: Function to log to both the console and the UI.
                    Log.d(
                        "[ChartboostCore]",
                        "Module ${result.module.moduleId} initialization completed with result: $result"
                    )
                    logs.value += "\nModule ${result.module.moduleId} initialization completed with result: $result"

                    iconTintMap[result.module.moduleId]?.value = CHARTBOOST_GREEN_COLOR
                }
            }
        )
    }
}
