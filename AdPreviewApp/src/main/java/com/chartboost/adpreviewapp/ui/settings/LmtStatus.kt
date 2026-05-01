/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.settings

import com.chartboost.adpreviewapp.R

enum class LmtStatus(val rawValue: String, val displayRes: Int) {
    GRANTED("granted", R.string.tracking_status_granted),
    DENIED("denied", R.string.tracking_status_denied),
    UNKNOWN("unknown", R.string.tracking_status_unknown),
    ;

    companion object {
        fun from(value: String): LmtStatus =
            entries.firstOrNull { it.rawValue.equals(value, ignoreCase = true) }
                ?: UNKNOWN
    }
}
