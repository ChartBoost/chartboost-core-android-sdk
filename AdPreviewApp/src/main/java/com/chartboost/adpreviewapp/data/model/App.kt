/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class App(
    val id: String,
    val nickname: String? = null,
    val icon: String? = null,
    val platform: String? = null,
    @SerialName("store_app_id") val storeAppId: String? = null,
    val name: String? = null,
)

@Serializable
data class AppsResponse(
    val items: List<App>,
)
