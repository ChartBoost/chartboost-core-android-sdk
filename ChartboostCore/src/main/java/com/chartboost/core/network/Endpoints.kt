/*
 * Copyright 2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.network

@Suppress
object Endpoints {
    /**
     * The Chartboost Core config endpoint. Modifiable via reflection for overriding purposes only.
     */
    internal var CORE_CONFIG = "https://config.core-sdk.chartboost.com/v1/core_config"
}
