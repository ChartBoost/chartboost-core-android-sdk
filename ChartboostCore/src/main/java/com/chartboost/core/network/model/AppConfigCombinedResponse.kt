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
data class AppConfigCombinedResponse(
    @SerialName("android")
    val config: AppConfigResponse? = null,
    // There's also an iOS side, but we don't care about it
)
