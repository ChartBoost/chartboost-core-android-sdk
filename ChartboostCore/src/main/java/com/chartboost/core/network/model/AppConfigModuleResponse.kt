/*
 * Copyright 2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Suppress
@Serializable
data class AppConfigModuleResponse(
    @SerialName("id")
    val id: String = "",
    @SerialName("className")
    val className: String = "",
    @SerialName("nonNativeClassName")
    val nonNativeClassName: String = "",
    @SerialName("config")
    val jsonConfig: JsonObject? = null,
)
