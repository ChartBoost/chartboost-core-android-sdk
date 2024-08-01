/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

/**
 * The listener for a [ConsentAdapter] to report changes to the [ConsentManagementPlatform]
 */
interface ConsentAdapterListener {
    /**
     * Called when any consent changes.
     *
     * @param consentKey The consent standard. See [ConsentKeys] for some defaults.
     */
    fun onConsentChange(consentKey: ConsentKey)
}
