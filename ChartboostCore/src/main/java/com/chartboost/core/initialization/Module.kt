/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.initialization

import android.content.Context
import org.json.JSONObject

/**
 * This interface is to be implemented by modules that can be initialized by [ChartboostCore].
 */
interface Module {
    /**
     * The ID of the module. This is recommended to be unique.
     */
    val moduleId: String

    /**
     * The version of the module. This is recommended to be a semantic version e.g. 1.0.0.
     */
    val moduleVersion: String

    /**
     * Updates the module with JSON data from the server. A publisher is recommended to
     * initialize via the constructor with module-specific parameters rather than using this function.
     * When creating a module, please make sure it's possible to send a JSONObject credentials
     * object to set up the parameters of this module.
     */
    fun updateCredentials(
        context: Context,
        credentials: JSONObject,
    )

    /**
     * Initialize the module.
     *
     * @param context The [Context] to use for initialization.
     * @param moduleConfiguration Configuration data for initializing the module.
     *
     * @return Result.success if the module was initialized successfully, Result.failure(Exception) otherwise.
     */
    suspend fun initialize(
        context: Context,
        moduleConfiguration: ModuleConfiguration,
    ): Result<Unit>
}
