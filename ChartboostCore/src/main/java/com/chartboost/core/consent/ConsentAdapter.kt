/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener

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
     * See [ConsentKeys] and [ConsentValues] for other
     * common examples.
     */
    val consents: Map<ConsentKey, ConsentValue>

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
    suspend fun showConsentDialog(
        activity: Activity,
        dialogType: ConsentDialogType,
    ): Result<Unit>

    /**
     * Grant user consent without showing a dialog.
     *
     * @param context The Android Context.
     * @param statusSource How this consent status was retrieved.
     *
     * @return Success Result or a Result with an Exception.
     */
    suspend fun grantConsent(
        context: Context,
        statusSource: ConsentSource,
    ): Result<Unit>

    /**
     * Deny user consent without showing a dialog.
     *
     * @param context The Android Context.
     * @param statusSource How this consent status was retrieved.
     *
     * @return Success Result or a Result with an Exception.
     */
    suspend fun denyConsent(
        context: Context,
        statusSource: ConsentSource,
    ): Result<Unit>

    /**
     * Reset user consent without showing a dialog. This usually also resets the underlying
     * consent management platform and re-initializes.
     *
     * @param context The Android Context.
     *
     * @return Success Result or a Result with an Exception.
     */
    suspend fun resetConsent(context: Context): Result<Unit>

    val sharedPreferencesIabStrings: MutableMap<String, String>
    val sharedPreferenceChangeListener: IabSharedPreferencesListener

    /**
     * Start observing all IAB prefixed privacy strings. It is not recommended to override this
     * method.
     *
     * @param context The Android Context.
     */
    fun startObservingSharedPreferencesIabStrings(context: Context) {
        val sharedPrefs =
            context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
        sharedPreferencesIabStrings.clear()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        sharedPrefs.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        ConsentKeys.FULL_SET.forEach { key ->
            sharedPrefs.getString(key, null)?.takeIf { it.isNotBlank() }?.let {
                sharedPreferencesIabStrings[key] = it
            }
        }
    }

    /**
     * Stop observing all IAB prefixed privacy strings. It is not recommended to override this
     * method.
     *
     * @param context The Android Context.
     */
    fun stopObservingSharedPreferencesIabStrings(context: Context) {
        val sharedPrefs =
            context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    /**
     * The default shared preferences listener that updates all IAB privacy strings.
     */
    class IabSharedPreferencesListener(
        private val sharedPreferencesIabStrings: MutableMap<String, String>,
    ) :
        OnSharedPreferenceChangeListener {
        var listener: ConsentAdapterListener? = null

        override fun onSharedPreferenceChanged(
            preferences: SharedPreferences?,
            key: String?,
        ) {
            if (key == null || preferences == null) {
                return
            }
            if (ConsentKeys.FULL_SET.contains(key)) {
                preferences.getString(key, null)?.let {
                    sharedPreferencesIabStrings[key] = it
                    listener?.onConsentChange(key)
                }
            }
        }
    }
}
