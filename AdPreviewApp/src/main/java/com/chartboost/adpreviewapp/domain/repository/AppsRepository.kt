/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.domain.repository

import com.chartboost.adpreviewapp.data.model.AdLocation
import com.chartboost.adpreviewapp.data.model.App

interface AppsRepository {
    suspend fun getApps(): List<App>

    suspend fun getAdLocations(appId: String): List<AdLocation>

    fun clearCache()
}
