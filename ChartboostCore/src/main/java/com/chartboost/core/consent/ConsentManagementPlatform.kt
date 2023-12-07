/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

import android.app.Activity
import android.content.Context
import com.chartboost.core.ChartboostCoreLogger
import com.chartboost.core.Utils
import com.chartboost.core.error.ChartboostCoreError
import com.chartboost.core.error.ChartboostCoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

typealias ConsentStandard = String
typealias ConsentValue = String

class ConsentManagementPlatform {
    /**
     * Whether or not it is appropriate to show a consent dialog.
     */
    var shouldCollectConsent: Boolean = false
        get() = adapter?.shouldCollectConsent ?: field
        private set

    /**
     * A collection of consent standards (eg. CCPA) and values (eg. 1YN-).
     */
    var consents: Map<ConsentStandard, ConsentValue> = mutableMapOf()
        get() = adapter?.consents ?: field
        private set

    /**
     * The current consent status of the user.
     */
    var consentStatus: ConsentStatus = ConsentStatus.UNKNOWN
        get() = adapter?.consentStatus ?: field
        private set

    /**
     * A map of partner IDs to their respective consent status.
     */
    var partnerConsentStatus: Map<String, ConsentStatus> = emptyMap()
        get() = adapter?.partnerConsentStatus ?: field
        private set

    private val observers: MutableSet<ConsentObserver> = mutableSetOf()
    private var adapter: ConsentAdapter? = null

    /**
     * Shows a consent dialog from the underlying consent management platform.
     *
     * @param activity The activity to attach the consent dialog.
     * @param dialogType The type of dialog to show.
     */
    suspend fun showConsentDialog(activity: Activity, dialogType: ConsentDialogType): Result<Unit> {
        ChartboostCoreLogger.d("Showing consent dialog.")
        return adapter?.showConsentDialog(activity, dialogType) ?: run {
            ChartboostCoreLogger.e("No adapter. Cannot show consent dialog.")
            Result.failure(ChartboostCoreException(ChartboostCoreError.ConsentError.DialogShowError))
        }
    }

    /**
     * Without showing a dialog, attempt to grant consent. This may have different effects
     * based on the underlying consent management platform. It is recommended to only use consent
     * dialogs to change consent status.
     *
     * @param context The Android Context.
     * @param statusSource How this consent status was retrieved.
     * @return Whether or not the operation succeeded
     */
    suspend fun grantConsent(context: Context, statusSource: ConsentStatusSource): Result<Unit> {
        ChartboostCoreLogger.d("Attempting to grant consent.")
        return adapter?.grantConsent(context, statusSource) ?: run {
            ChartboostCoreLogger.e("No CMP adapter to grant consent.")
            return Result.failure(ChartboostCoreException(ChartboostCoreError.ConsentError.MissingAdapter))
        }
    }

    /**
     * Attempt to deny consent without showing a consent dialog.
     *
     * @param context The Android Context.
     * @param statusSource How this consent status was retrieved.
     * @return Whether or not the operation succeeded
     */
    suspend fun denyConsent(context: Context, statusSource: ConsentStatusSource): Result<Unit> {
        ChartboostCoreLogger.d("Attempting to deny consent.")
        return adapter?.denyConsent(context, statusSource) ?: run {
            ChartboostCoreLogger.e("No CMP adapter to deny consent.")
            return Result.failure(ChartboostCoreException(ChartboostCoreError.ConsentError.MissingAdapter))
        }
    }

    /**
     * Reset the adapter and consent status. The underlying adapter may also reset and re-initialize.
     *
     * @param context The Android Context.
     * @return Whether or not the operation succeeded
     */
    suspend fun resetConsent(context: Context): Result<Unit> {
        ChartboostCoreLogger.d("Attempting to reset consent.")
        return adapter?.resetConsent(context) ?: run {
            ChartboostCoreLogger.e("No CMP adapter to reset consent.")
            return Result.failure(ChartboostCoreException(ChartboostCoreError.ConsentError.MissingAdapter))
        }
    }

    /**
     * Add an observer that listens to consent changes.
     *
     * @param observer The observer to add.
     */
    fun addObserver(observer: ConsentObserver) {
        CoroutineScope(Main.immediate).launch {
            observers.add(observer)
        }
    }

    /**
     * Remove an observer that listens to consent changes.
     *
     * @param observer The observer to remove.
     */
    fun removeObserver(observer: ConsentObserver) {
        CoroutineScope(Main.immediate).launch {
            observers.remove(observer)
        }
    }

    internal fun attachAdapter(consentAdapter: ConsentAdapter) {
        if (adapter != null) {
            ChartboostCoreLogger.d("Ignoring multiple consent adapter attach requests.")
            return
        }
        adapter = consentAdapter.apply {
            listener = object : ConsentAdapterListener {
                override fun onConsentStatusChange(status: ConsentStatus) {
                    Utils.safeExecute {
                        observers.forEach {
                            it.onConsentStatusChange(status)
                        }
                    }
                }

                override fun onConsentChangeForStandard(
                    standard: ConsentStandard, value: ConsentValue?
                ) {
                    Utils.safeExecute {
                        observers.forEach {
                            it.onConsentChangeForStandard(standard, value)
                        }
                    }
                }

                override fun onPartnerConsentStatusChange(
                    partnerId: String,
                    status: ConsentStatus
                ) {
                    Utils.safeExecute {
                        observers.forEach {
                            it.onPartnerConsentStatusChange(partnerId, status)
                        }
                    }
                }
            }
        }
        Utils.safeExecute {
            observers.forEach {
                it.onConsentModuleReady()
            }
        }
    }

    internal fun isAdapterAttached(): Boolean {
        return adapter != null
    }
}
