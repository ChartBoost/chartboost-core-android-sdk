/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.canary

import com.chartboost.core.initialization.InitializableModule
import kotlin.reflect.full.memberProperties

/**
 * Utility functions for Canary.
 */
object Utils {
    /**
     * Get the declared properties for a module.
     *
     * @param modules The map of module names to module instances.
     * @param moduleId The ID of the module to get the declared properties for.
     *
     * @return A map of property names to property values (empty strings to be filled in by the user).
     */
    fun getDeclaredPropsForModule(
        modules: Map<String, InitializableModule>,
        moduleId: String,
    ): Map<String, String> {
        val moduleInstance = modules[moduleId] ?: return emptyMap()
        return moduleInstance::class.memberProperties
            .map { it.name }
            .associateWith { "" }
    }
}
