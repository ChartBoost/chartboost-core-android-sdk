/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.initialization

import com.chartboost.core.ChartboostCoreResult
import com.chartboost.core.error.ChartboostCoreException
import org.json.JSONObject

/**
 * Result of a module initialization.
 */
open class ModuleInitializationResult(
    override val start: Long,
    override val end: Long,
    override val duration: Long,
    override val exception: ChartboostCoreException?,
    val module: InitializableModule,
) : ChartboostCoreResult(start, end, duration, exception) {
    /**
     * Returns a string representation of the result.
     */
    override fun toString() =
        "${super.toString()}, ChartboostCoreModuleInitializationResult(moduleId=${module.moduleId}, moduleVersion=${module.moduleVersion})"

    /**
     * Returns a JSON representation of the result.
     */
    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put("moduleId", module.moduleId)
            put("moduleVersion", module.moduleVersion)
        }
    }
}
