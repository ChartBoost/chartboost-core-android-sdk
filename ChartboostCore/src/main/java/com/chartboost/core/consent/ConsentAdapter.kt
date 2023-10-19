/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

import android.app.Activity
import android.content.Context

/**
 * The consent management platform adapter to communicate between Chartboost Core and the CMP.
 */
interface ConsentAdapter {
    /**
     * This should tell the publisher whether or not to show a consent dialog.
     */
    val shouldCollectConsent: Boolean

    /**
     * Map of consent standards (eg. GDPR, USP, etc) to the consent value (eg. GRANTED, 1YN-, etc).
     * See [DefaultConsentStandard] and [DefaultConsentValue] for other
     * common examples.
     */
    val consents: Map<ConsentStandard, ConsentValue>

    /**
     * The overall consent status.
     */
    val consentStatus: ConsentStatus

    /**
     * If any consent standards or status changes happen, notify this listener.
     */
    var listener: ConsentAdapterListener?

    /**
     * Show a consent dialog.
     *
     * @param activity The activity to attach the consent dialog.
     * @param dialogType The type of dialog to show.
     *
     * @return Success Result or a Result with an Exception.
     */
    suspend fun showConsentDialog(activity: Activity, dialogType: ConsentDialogType): Result<Unit>

    /**
     * Grant user consent without showing a dialog.
     *
     * @param context The Android Context.
     * @param statusSource How this consent status was retrieved.
     *
     * @return Success Result or a Result with an Exception.
     */
    suspend fun grantConsent(context: Context, statusSource: ConsentStatusSource): Result<Unit>

    /**
     * Deny user consent without showing a dialog.
     *
     * @param context The Android Context.
     * @param statusSource How this consent status was retrieved.
     *
     * @return Success Result or a Result with an Exception.
     */
    suspend fun denyConsent(context: Context, statusSource: ConsentStatusSource): Result<Unit>

    /**
     * Reset user consent without showing a dialog. This usually also resets the underlying
     * consent management platform and re-initializes.
     *
     * @param context The Android Context.
     *
     * @return Success Result or a Result with an Exception.
     */
    suspend fun resetConsent(context: Context): Result<Unit>
}
