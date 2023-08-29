/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

enum class DefaultConsentValue(val value: String) {
    GRANTED("granted"),
    DENIED("denied"),
    DOES_NOT_APPLY("does_not_apply")
}
