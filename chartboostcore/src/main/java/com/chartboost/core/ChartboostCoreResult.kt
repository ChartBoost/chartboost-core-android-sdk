/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import com.chartboost.core.error.ChartboostCoreException
import org.json.JSONObject

/**
 * @suppress
 *
 * Basic components of a result.
 *
 * @property start The start time of the operation.
 * @property end The end time of the operation.
 * @property duration The duration of the operation.
 * @property exception The exception thrown during the operation.
 */
open class ChartboostCoreResult(
    open val start: Long,
    open val end: Long,
    open val duration: Long,
    open val exception: ChartboostCoreException? = null,
) {
    /**
     * Returns a string representation of the result
     */
    override fun toString() =
        "ChartboostCoreResult(start=$start, end=$end, duration=$duration, exception=${exception?.message})"

    /**
     * Returns a JSON representation of the result
     */
    open fun toJson(): JSONObject {
        return JSONObject().apply {
            put("start", start)
            put("end", end)
            put("duration", duration)
            exception?.message?.let {
                put("exception", it)
            }
        }
    }
}
