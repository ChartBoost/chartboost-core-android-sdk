/*
 * Copyright 2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.initialization

import android.content.Context
import com.chartboost.core.ChartboostCoreLogger
import com.chartboost.core.Utils
import com.chartboost.core.network.ChartboostCoreJson
import com.chartboost.core.network.model.AppConfigResponse
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer

/**
 * Handles getting the Core init config from the backend and handles caching the result.
 */
@Suppress
class InitConfigController {
    companion object {
        private const val CHARTBOOST_CORE_CONFIG_IDENTIFIER = "com.chartboost.core.config"
    }

    internal suspend fun fetchConfig(context: Context): AppConfigResponse? {
        val combinedResponseResult = Utils.fetchInitializationDataFromBackend()
        val appConfigResponse = combinedResponseResult.getOrNull()?.config
        var appConfigReturnValue = appConfigResponse ?: getConfigFromSharedPreferences(context)
        if (appConfigReturnValue == null) {
            ChartboostCoreLogger.d("Failed to find local or remote ChartboostCore config. Starting task to fetch")

            Utils.executeWithExponentialBackoff {
                Utils.fetchInitializationDataFromBackend()
            }.getOrNull()?.config?.let {
                appConfigReturnValue = it
                saveConfigToSharedPreferences(context, it)
            }
        } else {
            saveConfigToSharedPreferences(context, appConfigReturnValue)
        }
        return appConfigReturnValue
    }

    private suspend fun getConfigFromSharedPreferences(context: Context): AppConfigResponse? =
        withContext(IO) {
            ChartboostCoreLogger.d("Attempting to get ChartboostCore config from shared preferences.")

            val sharedPreferences =
                context.getSharedPreferences(
                    CHARTBOOST_CORE_CONFIG_IDENTIFIER,
                    Context.MODE_PRIVATE,
                )

            try {
                sharedPreferences.getString(CHARTBOOST_CORE_CONFIG_IDENTIFIER, null)?.let {
                    ChartboostCoreJson.decodeFromString(serializer(), it)
                }
            } catch (exception: SerializationException) {
                ChartboostCoreLogger.w("Unable to create a ChartboostCore config from shared preferences: $exception")
                null
            }
        }

    private suspend fun saveConfigToSharedPreferences(
        context: Context,
        appConfigResponse: AppConfigResponse?,
    ) = withContext(IO) {
        if (appConfigResponse == null) {
            ChartboostCoreLogger.d("Ignoring trying to save a null ChartboostCore config.")
            return@withContext
        }
        val sharedPreferences =
            context.getSharedPreferences(
                CHARTBOOST_CORE_CONFIG_IDENTIFIER,
                Context.MODE_PRIVATE,
            )

        sharedPreferences.edit().putString(
            CHARTBOOST_CORE_CONFIG_IDENTIFIER,
            ChartboostCoreJson.encodeToString(serializer(), appConfigResponse),
        ).apply()
        ChartboostCoreLogger.d("ChartboostCore config saved to disk successfully.")
    }
}
