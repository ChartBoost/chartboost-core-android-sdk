/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

/**
 * Some common consent keys.
 */
object ConsentKeys {
    /**
     * Whether or not GDPR consent is given.
     */
    const val GDPR_CONSENT_GIVEN: ConsentKey = "chartboost_core_gdpr_consent_given"

    /**
     * Whether or not the user has opted into CCPA.
     */
    const val CCPA_OPT_IN = "chartboost_core_ccpa_opt_in"

    /**
     * The US Privacy String.
     */
    const val USP = "IABUSPrivacy_String"

    /**
     * The Interactive Advertising Bureau Transparency & Consent Framework String.
     */
    const val TCF = "IABTCF_TCString"

    /**
     * The Interactive Advertising Bureau Global Privacy Platform String.
     */
    const val GPP = "IABGPP_HDR_GppString"

    /**
     * A Set of all [ConsentKeys].
     */
    val FULL_SET = setOf(GDPR_CONSENT_GIVEN, CCPA_OPT_IN, USP, TCF, GPP)
}
