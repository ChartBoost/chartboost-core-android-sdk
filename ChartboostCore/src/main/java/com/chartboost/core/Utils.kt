/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import com.chartboost.core.Constants.MAX_RETRY_ATTEMPTS
import com.chartboost.core.Constants.MAX_RETRY_DELAY_MS
import com.chartboost.core.Constants.SEED_RETRY_DELAY_MS
import com.chartboost.core.error.ChartboostCoreError
import com.chartboost.core.error.ChartboostCoreException
import com.chartboost.core.network.ChartboostCoreNetworking
import com.chartboost.core.network.model.AppConfigCombinedResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.json.JSONObject
import kotlin.math.min

/**
 * @suppress
 *
 * Utilities for Chartboost Core.
 */
object Utils {
    /**
     * Execute a block of code with exponential backoff until it succeeds or the maximum number of attempts is reached.
     *
     * @param action The action to perform.
     *
     * @return The result of the action.
     */
    suspend fun <T> executeWithExponentialBackoff(action: suspend (retryCount: Int) -> Result<T>): Result<T> {
        var currentAttempt = 0
        var delayMs = SEED_RETRY_DELAY_MS

        while (currentAttempt <= MAX_RETRY_ATTEMPTS) {
            try {
                val result = action(currentAttempt)
                if (result.isSuccess) {
                    return result
                }
            } catch (e: Exception) {
                ChartboostCoreLogger.e(
                    "Exception occurred while retrying an action with exponential " +
                        "backoff in retry attempt $currentAttempt: $e",
                )
            }

            delay(delayMs)
            currentAttempt++
            delayMs = min(MAX_RETRY_DELAY_MS, delayMs * 2)
        }

        return Result.failure(ChartboostCoreException(ChartboostCoreError.CoreError.ExponentialBackoffRetriesExhausted))
    }

    /**
     * Safely execute a block of code in a new coroutine and log any exception that it throws.
     *
     * @param dispatcher The dispatcher to use. Main by default.
     * @param block The block of code to execute.
     */
    fun safeExecute(
        dispatcher: CoroutineDispatcher = Main,
        block: suspend () -> Unit,
    ) {
        CoroutineScope(dispatcher).launch(
            CoroutineExceptionHandler { _, throwable ->
                ChartboostCoreLogger.e("Error occurred during safe execution: $throwable")
            },
        ) {
            try {
                block()
            } catch (e: Exception) {
                ChartboostCoreLogger.e("Error occurred during safe execution: $e")
            }
        }
    }

    /**
     * Fetches the Core config from the backend.
     *
     * @return The result of the operation which contains an [AppConfigCombinedResponse] or an error.
     */
    suspend fun fetchInitializationDataFromBackend(): Result<AppConfigCombinedResponse?> {
        return withContext(IO) {
            try {
                ChartboostCoreNetworking.getCoreConfig()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Converts a kotlinx JsonObject to a JSONObject.
     */
    @Throws(SerializationException::class, IllegalArgumentException::class)
    fun JsonObject.toJSONObject(): JSONObject = JSONObject(Json.encodeToString(this))
}
