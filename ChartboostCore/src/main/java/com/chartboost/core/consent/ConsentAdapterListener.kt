/*
 * Copyright 2023 Chartboost, Inc.
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
     * Called when the consent status changes.
     *
     * @param status The new consent status.
     */
    fun onConsentStatusChange(status: ConsentStatus)

    /**
     * Called when any consent standard changes.
     *
     * @param standard The consent standard. See [DefaultConsentStandard] for some defaults.
     * @param value The consent value. See [DefaultConsentValue] for some defaults. This can be null.
     */
    fun onConsentChangeForStandard(standard: ConsentStandard, value: ConsentValue?)

    /**
     * Called when the consent status changes for a specific partner.
     *
     * @param partnerId The partner ID. See Chartboost documentation for more details.
     * @param status The new consent status.
     */
    fun onPartnerConsentStatusChange(partnerId: String, status: ConsentStatus)
}
