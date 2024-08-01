/*
 * Copyright 2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

enum class ChartboostCoreLogLevel(val value: Int) {
    DISABLED(0),
    ERROR(1),
    WARNING(2),
    INFO(3),
    DEBUG(4),
    VERBOSE(5),
    ;

    companion object {
        /**
         * Get the log level corresponding to the integer value or VERBOSE if nothing matches.
         *
         * @param logLevelInt Integer representation of the log level.
         */
        fun fromInt(logLevelInt: Int?): ChartboostCoreLogLevel {
            return entries.find { it.value == logLevelInt } ?: VERBOSE
        }
    }
}
