/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

import android.content.Context

/**
 * Modules that implement this interface are automatically subscribed to consent updates. Use this
 * interface to subscribe to consent changes via the [ConsentManagementPlatform].
 */
interface ConsentObserver {
    /**
     * Called when the Consent Management Platform adapter is attached and ready to be used.
     *
     * @param appContext The application context.
     * @param initialConsents All of the consents available when the module is ready.
     * See [ConsentKeys] and [ConsentValues] for some defaults.
     */
    fun onConsentModuleReady(
        appContext: Context,
        initialConsents: Map<ConsentKey, ConsentValue>,
    )

    /**
     * Called when any consent standard changes.
     *
     * @param appContext The application context.
     * @param fullConsents All of the consents. If a key is present but there is no value, the value
     *                     has been removed. See [DefaultConsentKey] and [DefaultConsentValue] for some defaults.
     * @param modifiedKeys The set of consents that have changed. See [ConsentKeys] for some defaults.
     */
    fun onConsentChange(
        appContext: Context,
        fullConsents: Map<ConsentKey, ConsentValue>,
        modifiedKeys: Set<ConsentKey>,
    )
}
