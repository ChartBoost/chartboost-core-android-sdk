/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

/**
 * Modules that implement this interface are automatically subscribed to consent updates. Use this
 * interface to subscribe to consent changes via the [ConsentManagementPlatform].
 */
interface ConsentObserver {
    /**
     * Called when the Consent Management Platform adapter is attached and ready to be used.
     */
    fun onConsentModuleReady()

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
     * Called when a partner's consent status changes. See the Chartboost documentation for a
     * list of partner IDs.
     *
     * @param partnerId The partner ID.
     * @param status The new consent status.
     */
    fun onPartnerConsentStatusChange(partnerId: String, status: ConsentStatus)
}
