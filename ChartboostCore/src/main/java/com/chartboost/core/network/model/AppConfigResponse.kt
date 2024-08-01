/*
 * Copyright 2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress
@Serializable
data class AppConfigResponse(
    @SerialName("modules")
    val modules: List<AppConfigModuleResponse>,
    @SerialName("logLevel")
    val logLevel: Int?,
)
