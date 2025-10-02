/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.di

import com.chartboost.adpreviewapp.BuildConfig
import com.chartboost.adpreviewapp.data.api.AdPreviewApi
import com.chartboost.adpreviewapp.data.auth.AccessTokenProvider
import com.chartboost.adpreviewapp.data.auth.AccessTokenProviderImpl
import com.chartboost.adpreviewapp.data.sdk.ChartboostSdkInitializerImpl
import com.chartboost.adpreviewapp.data.sdk.ChartboostSdkTestModeSwitcherImpl
import com.chartboost.adpreviewapp.data.system.LmtStatusCheckerImpl
import com.chartboost.adpreviewapp.domain.sdk.ChartboostSdkInitializer
import com.chartboost.adpreviewapp.domain.sdk.ChartboostSdkTestModeSwitcher
import com.chartboost.adpreviewapp.domain.system.LmtStatusChecker
import com.chartboost.adpreviewapp.service.SystemCredentialsService
import com.chartboost.adpreviewapp.service.SystemCredentialsServiceImpl
import com.chartboost.adpreviewapp.ui.testscreen.AdEventsLogger
import com.chartboost.adpreviewapp.util.DispatcherProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindAccessTokenProvider(impl: AccessTokenProviderImpl): AccessTokenProvider

    @Binds
    @Singleton
    abstract fun bindChartboostSdkInitializer(impl: ChartboostSdkInitializerImpl): ChartboostSdkInitializer

    @Binds
    abstract fun bindTestModeSwitcher(impl: ChartboostSdkTestModeSwitcherImpl): ChartboostSdkTestModeSwitcher

    @Binds
    @Singleton
    abstract fun bindLmtStatusChecker(impl: LmtStatusCheckerImpl): LmtStatusChecker

    @Binds
    @Singleton
    abstract fun bindSystemCredendtialsService(impl: SystemCredentialsServiceImpl): SystemCredentialsService

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(accessTokenProvider: AccessTokenProvider): OkHttpClient =
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    val token = accessTokenProvider.getToken()

                    val updatedRequest =
                        if (!token.isNullOrBlank()) {
                            request.newBuilder()
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                        } else {
                            request
                        }

                    chain.proceed(updatedRequest)
                }
                .build()

        @Provides
        @Singleton
        fun provideRetrofit(client: OkHttpClient): Retrofit {
            val json =
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    prettyPrint = BuildConfig.DEBUG
                }

            val contentType = "application/json".toMediaType()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(json.asConverterFactory(contentType))
                .client(client)
                .build()
        }

        @Provides
        @Singleton
        fun provideAdPreviewApi(retrofit: Retrofit): AdPreviewApi = retrofit.create(AdPreviewApi::class.java)

        @Provides
        @Singleton
        fun provideDispatcherProvider(): DispatcherProvider =
            object : DispatcherProvider {
                override fun main(): CoroutineDispatcher {
                    return Dispatchers.Main
                }

                override fun io(): CoroutineDispatcher {
                    return Dispatchers.IO
                }

                override fun default(): CoroutineDispatcher {
                    return Dispatchers.Default
                }

                override fun unconfined(): CoroutineDispatcher {
                    return Dispatchers.Unconfined
                }
            }

        @Provides
        fun provideAdEventsLogger(): AdEventsLogger = AdEventsLogger()

        @Provides
        @IsDebugFlag
        fun provideIsDebug(): Boolean = BuildConfig.DEBUG

        @Provides
        @MonetizationSdkVersion
        fun provideMonetizationSdkVersion(): String = BuildConfig.MONETIZATION_SDK_VERSION
    }
}
