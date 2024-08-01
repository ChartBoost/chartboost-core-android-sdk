/*
 * Copyright 2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.network

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Url

@Suppress
interface ChartboostCoreApi {
    @POST
    suspend fun getCoreConfig(
        @Url url: String,
    ): Response<String>
}
