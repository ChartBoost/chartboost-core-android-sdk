/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

enum class DefaultConsentStandard(val value: String) {
    GDPR_CONSENT_GIVEN("gdpr_consent_given"),
    CCPA_OPT_IN("ccpa_opt_in"),
    USP("usp"),
    TCF("tcf"),
    GPP("gpp"),
}
