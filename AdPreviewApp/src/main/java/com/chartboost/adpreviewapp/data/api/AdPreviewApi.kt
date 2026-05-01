/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.api

import com.chartboost.adpreviewapp.data.model.AdLocationsResponse
import com.chartboost.adpreviewapp.data.model.AppsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AdPreviewApi {
    @GET("apps")
    suspend fun getApps(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 24,
        @Query("sort") sort: String = "date_created",
        @Query("direction") direction: String = "desc",
    ): AppsResponse

    @GET("/apps/{appId}/ad-locations")
    suspend fun getLocationsForApp(
        @Path("appId") appId: String,
    ): AdLocationsResponse
}
