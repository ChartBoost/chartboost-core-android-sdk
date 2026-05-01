/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.domain.system

import android.os.Build

fun isAmazonDevice(): Boolean {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL

    return manufacturer.equals("Amazon", ignoreCase = true) ||
        model.startsWith("AFT", ignoreCase = true) || // Fire TV devices
        model.startsWith("KF", ignoreCase = true) // Kindle Fire tablets
}
