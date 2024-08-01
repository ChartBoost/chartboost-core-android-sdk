/*
 * Copyright 2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.network

import androidx.annotation.VisibleForTesting
import com.chartboost.core.ChartboostCoreLogLevel
import com.chartboost.core.ChartboostCoreLogger
import com.chartboost.core.error.ChartboostCoreError
import com.chartboost.core.error.ChartboostCoreException
import com.chartboost.core.error.coreErrorSerializersModule
import com.chartboost.core.network.model.AppConfigCombinedResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.UnknownHostException

@Suppress
object ChartboostCoreNetworking {
    /**
     * Custom interceptor that is settable via reflection for debugging purposes
     */
    private var customInterceptor: Interceptor? = null

    private val jsonConverter =
        ChartboostCoreJson.asConverterFactory(
            "application/json; charset=utf-8".toMediaType(),
        )

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()

        // Apply the logging interceptor if the log level is appropriate
        builder.addInterceptor(createHttpLoggingInterceptor())

        // Apply custom interceptor if it's provided
        customInterceptor?.let { builder.addInterceptor(it) }
        builder.build()
    }

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    internal const val BASE_DOMAIN = "https://chartboost.com"

    // by lazy is necessary for changing the url for testing
    @VisibleForTesting
    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_DOMAIN)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(jsonConverter)
            .build()
    }

    // by lazy is necessary for changing the url for testing
    @VisibleForTesting
    val api by lazy {
        retrofitInstance.create(ChartboostCoreApi::class.java)
    }

    suspend fun getCoreConfig(): Result<AppConfigCombinedResponse?> {
        return safeApiCall<AppConfigCombinedResponse> {
            api.getCoreConfig(
                url = Endpoints.CORE_CONFIG,
            )
        }
    }

    private fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logLevel =
            when (ChartboostCoreLogger.logLevel) {
                ChartboostCoreLogLevel.VERBOSE, ChartboostCoreLogLevel.DEBUG -> HttpLoggingInterceptor.Level.BODY
                ChartboostCoreLogLevel.INFO -> HttpLoggingInterceptor.Level.BASIC
                ChartboostCoreLogLevel.WARNING -> HttpLoggingInterceptor.Level.HEADERS
                ChartboostCoreLogLevel.ERROR, ChartboostCoreLogLevel.DISABLED -> HttpLoggingInterceptor.Level.NONE
            }

        return HttpLoggingInterceptor { message ->
            // Only log if the configured log level is INFO or more verbose
            if (ChartboostCoreLogger.logLevel.value >= ChartboostCoreLogLevel.INFO.value) {
                Platform.get().log(message)
            }
        }.apply {
            level = logLevel
        }
    }

    private suspend inline fun <reified T> safeApiCall(crossinline apiCall: suspend () -> Response<String>): Result<T?> {
        return withContext(ioDispatcher) {
            try {
                val response = apiCall.invoke()

                if (response.isSuccessful) {
                    Result.success(
                        ChartboostCoreJson.decodeFromString<T>(
                            serializer(),
                            response.body() ?: "",
                        ),
                    )
                } else {
                    Result.failure(ChartboostCoreException(ChartboostCoreError.CoreError.UnknownNetworkingError))
                }
            } catch (throwable: Throwable) {
                ChartboostCoreLogger.i("Error making network request: ${throwable.message}")
                if (throwable is UnknownHostException) {
                    Result.failure(ChartboostCoreException(ChartboostCoreError.CoreError.NoConnectivityError))
                } else if (throwable is SerializationException) {
                    Result.failure(ChartboostCoreException(ChartboostCoreError.CoreError.SerializationError))
                } else {
                    Result.failure(ChartboostCoreException(ChartboostCoreError.CoreError.UnknownNetworkingError))
                }
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal val ChartboostCoreJson =
    Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        serializersModule =
            SerializersModule {
                include(coreErrorSerializersModule)
            }
    }
