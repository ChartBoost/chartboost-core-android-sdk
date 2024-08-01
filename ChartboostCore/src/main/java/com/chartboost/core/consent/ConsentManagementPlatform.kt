/*
 * Copyright 2023-2024 Chartboost, Inc.
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * ConsentKey is just a marker to say the intended keys for this are very specific IAB strings.
 */
typealias ConsentKey = String

/**
 * ConsentValue is a marker to say that this String is actually some kind of consent data.
 */
typealias ConsentValue = String

/**
 * Handles consent for all modules and for the publisher.
 */
class ConsentManagementPlatform {
    companion object {
        /**
         * The default amount of time to wait to send consent updates to debounce consent changes.
         */
        const val DEFAULT_CONSENT_UPDATE_BATCH_DELAY_MS = 500L

        /**
         * Convenience method to check US privacy consent based on the USP String.
         *
         * @param uspString The US Privacy String
         * @return True if consent is given, false otherwise
         */
        fun getUspConsentFromUspString(uspString: String?): Boolean {
            return uspString?.getOrNull(2) == 'N'
        }
    }

    /**
     * Whether or not it is appropriate to show a consent dialog.
     */
    var shouldCollectConsent: Boolean = false
        get() = adapter?.shouldCollectConsent ?: field
        private set

    /**
     * A collection of consent standards (eg. CCPA) and values (eg. 1YN-).
     */
    var consents: Map<ConsentKey, ConsentValue> = mutableMapOf()
        get() = (adapter?.consents ?: field).toMap()
        private set

    /**
     * How many milliseconds to wait when batching consent updates.
     * Default is [DEFAULT_CONSENT_UPDATE_BATCH_DELAY_MS].
     */
    var consentUpdateBatchDelayMs = DEFAULT_CONSENT_UPDATE_BATCH_DELAY_MS

    /**
     * Consent updates these observers.
     */
    private val observers: MutableSet<ConsentObserver> = mutableSetOf()

    /**
     * The adapter class for the actual underlying consent sdk.
     */
    private var adapter: ConsentAdapter? = null

    /**
     * Shows a consent dialog from the underlying consent management platform.
     *
     * @param activity The activity to attach the consent dialog.
     * @param dialogType The type of dialog to show.
     */
    suspend fun showConsentDialog(
        activity: Activity,
        dialogType: ConsentDialogType,
    ): Result<Unit> {
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
    suspend fun grantConsent(
        context: Context,
        statusSource: ConsentSource,
    ): Result<Unit> {
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
    suspend fun denyConsent(
        context: Context,
        statusSource: ConsentSource,
    ): Result<Unit> {
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

    /**
     * Attaches an adapter to the consent management platform.
     *
     * @param appContext The application context.
     * @param consentAdapter The Consent adapter.
     */
    internal fun attachAdapter(
        appContext: Context,
        consentAdapter: ConsentAdapter,
    ) {
        if (adapter != null) {
            ChartboostCoreLogger.d("Ignoring multiple consent adapter attach requests.")
            return
        }
        adapter =
            consentAdapter.apply {
                listener = DebouncingConsentAdapterListener(appContext, this@ConsentManagementPlatform)
            }
        Utils.safeExecute {
            observers.forEach {
                it.onConsentModuleReady(appContext, consents)
            }
        }
    }

    /**
     * Whether or not there already is an adapter attached.
     */
    internal fun isAdapterAttached(): Boolean {
        return adapter != null
    }

    /**
     * This class automatically gathers consent changes and emits the onConsentChange callback
     * after [consentUpdateBatchDelayMs] so consent changes comes all at once.
     */
    class DebouncingConsentAdapterListener(
        private val appContext: Context,
        private val consentManagementPlatform: ConsentManagementPlatform,
    ) :
        ConsentAdapterListener {
        private val changedConsents: MutableSet<ConsentKey> =
            Collections.synchronizedSet(mutableSetOf())
        private var consentChangeJob: Job? = null

        override fun onConsentChange(consentKey: ConsentKey) {
            changedConsents.add(consentKey)
            if (consentChangeJob == null) {
                consentChangeJob =
                    CoroutineScope(Main).launch {
                        delay(consentManagementPlatform.consentUpdateBatchDelayMs)
                        Utils.safeExecute {
                            consentManagementPlatform.observers.forEach {
                                it.onConsentChange(appContext, consentManagementPlatform.consents, changedConsents.toSet())
                            }
                            changedConsents.clear()
                            consentChangeJob = null
                        }
                    }
            }
        }
    }
}
