/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.repository

import com.chartboost.adpreviewapp.data.api.AdPreviewApi
import com.chartboost.adpreviewapp.data.model.AdLocation
import com.chartboost.adpreviewapp.data.model.App
import com.chartboost.adpreviewapp.domain.repository.AppsRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppsRepositoryImpl
    @Inject
    constructor(
        private val api: AdPreviewApi,
    ) : AppsRepository {
        // A dedicated data class for cache entries
        private data class CacheEntry<T>(val data: T, val timestamp: Long)

        // In-memory cache for the list of apps.
        private var appsCache: CacheEntry<List<App>>? = null

        // In-memory cache for ad locations, using the app's ID as the key.
        private val adLocationsCache = mutableMapOf<String, CacheEntry<List<AdLocation>>>()

        override suspend fun getApps(): List<App> {
            val cachedApps = appsCache
            if (cachedApps != null && !isCacheExpired(cachedApps.timestamp)) {
                return cachedApps.data
            }

            val platformFilteredApps =
                api.getApps().items.filter {
                    it.platform in setOf(ANDROID_PLATFORM_NAME, AMAZON_PLATFORM_NAME)
                }

            appsCache = CacheEntry(data = platformFilteredApps, timestamp = System.currentTimeMillis())
            return platformFilteredApps
        }

        override suspend fun getAdLocations(appId: String): List<AdLocation> {
            val cachedAdLocations = adLocationsCache[appId]
            if (cachedAdLocations != null && !isCacheExpired(cachedAdLocations.timestamp)) {
                return cachedAdLocations.data
            }

            val newAdLocations = api.getLocationsForApp(appId).items
            adLocationsCache[appId] = CacheEntry(data = newAdLocations, timestamp = System.currentTimeMillis())
            return newAdLocations
        }

        override fun clearCache() {
            appsCache = null
            adLocationsCache.clear()
        }

        private fun isCacheExpired(timestamp: Long): Boolean {
            return (System.currentTimeMillis() - timestamp) > CACHE_EXPIRATION_MS
        }

        companion object {
            private val CACHE_EXPIRATION_MS = TimeUnit.MINUTES.toMillis(5) // 5 minutes
            private const val AMAZON_PLATFORM_NAME = "amazon"
            private const val ANDROID_PLATFORM_NAME = "android"
        }
    }
