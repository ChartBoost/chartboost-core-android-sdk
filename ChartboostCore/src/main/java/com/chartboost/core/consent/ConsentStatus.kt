/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

enum class ConsentStatus(val value: String) {
    GRANTED("granted"),
    DENIED("denied"),
    UNKNOWN("unknown"),
    ;

    override fun toString() = value

    companion object {
        /**
         * Generates a [ConsentStatus] using the String value of the enum. If a ConsentStatus
         * cannot be made from the input String, then it returns UNKNOWN.
         *
         * @param stringValue The input String value
         * @return A [ConsentStatus] that has the String value as the input.
         */
        fun fromString(stringValue: String): ConsentStatus {
            return try {
                valueOf(stringValue.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}
