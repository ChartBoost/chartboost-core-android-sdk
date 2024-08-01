/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

/**
 * The various network connection types known to ChartboostCore. See
 * [ConnectivityManager](https://developer.android.com/reference/android/net/ConnectivityManager)
 * for more information.
 */
enum class NetworkConnectionType {
    UNKNOWN,
    WIRED,
    WIFI,
    CELLULAR_UNKNOWN,
    CELLULAR_2G,
    CELLULAR_3G,
    CELLULAR_4G,
    CELLULAR_5G,
}
