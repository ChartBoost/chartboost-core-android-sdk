/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import com.chartboost.core.error.ChartboostCoreException
import com.chartboost.core.initialization.InitializableModule
import com.chartboost.core.initialization.ModuleInitializationResult
import com.chartboost.core.initialization.SdkInitializationResult

/**
 * This object is responsible for tracking results for [ChartboostCore] and its modules.
 */
internal object ResultManager {
    /**
     * Map of module name to result data for tracking purposes.
     */
    val resultDataMap = mutableMapOf<InitializableModule, ResultData>()

    /**
     * Start tracking result for a module.
     */
    fun start(module: InitializableModule) {
        val resultData = ResultData(module)
        resultData.start()
        resultDataMap[module] = resultData
    }

    /**
     * Stop tracking result for a module.
     */
    fun stop(
        module: InitializableModule,
        exception: ChartboostCoreException? = null
    ): ChartboostCoreResult {
        val resultData = resultDataMap.getOrElse(module) {
            ChartboostCoreLogger.e("No result data found for module $module. Creating new one.")
            ResultData(module)
        }
        resultData.stop(exception)
        return resultData.build()
    }

    /**
     * Data class for tracking result for a module.
     *
     * @property module The module instance.
     */
    class ResultData(private val module: InitializableModule) {
        private var startTime: Long = System.currentTimeMillis()
        private var endTime: Long? = null
        private var exception: ChartboostCoreException? = null

        /**
         * Start tracking result.
         */
        fun start() {
            startTime = System.currentTimeMillis()
        }

        /**
         * Stop tracking result.
         *
         * @param exception The exception that occurred during the module's operation
         */
        fun stop(exception: ChartboostCoreException?) {
            endTime = System.currentTimeMillis()
            this.exception = exception
        }

        /**
         * Build a [ChartboostCoreResult] from the tracked result.
         */
        fun build(): ChartboostCoreResult {
            val updatedEndTime = endTime ?: System.currentTimeMillis()
            return if (module is ChartboostCoreModule) {
                SdkInitializationResult(
                    start = startTime,
                    end = updatedEndTime,
                    duration = updatedEndTime.minus(startTime),
                    exception = exception,
                )
            } else {
                ModuleInitializationResult(
                    start = startTime,
                    end = updatedEndTime,
                    duration = updatedEndTime.minus(startTime),
                    exception = exception,
                    module = module,
                )
            }
        }
    }
}
