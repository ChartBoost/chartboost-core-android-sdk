/*
 * Copyright 2023-2024 Chartboost, Inc.
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
    val moduleId: String,
    val moduleVersion: String,
) : ChartboostCoreResult(start, end, duration, exception) {
    /**
     * Returns a string representation of the result.
     */
    override fun toString() =
        "${super.toString()}, ChartboostCoreModuleInitializationResult(moduleId=$moduleId, moduleVersion=$moduleVersion)"

    /**
     * Returns a JSON representation of the result.
     */
    override fun toJson(): JSONObject {
        return super.toJson().apply {
            put("moduleId", moduleId)
            put("moduleVersion", moduleVersion)
        }
    }
}
