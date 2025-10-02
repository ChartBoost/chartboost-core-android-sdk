/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen.banner

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chartboost.adpreviewapp.di.IsDebugFlag
import com.chartboost.adpreviewapp.domain.SettingsPreferences
import com.chartboost.adpreviewapp.domain.repository.KeywordsRepository
import com.chartboost.adpreviewapp.ui.testscreen.AdEventsLogger
import com.chartboost.adpreviewapp.ui.testscreen.CallbackChipState
import com.chartboost.adpreviewapp.ui.utils.model.LoadingButtonState
import com.chartboost.chartboostmediationsdk.ChartboostMediationIlrdObserver
import com.chartboost.chartboostmediationsdk.ChartboostMediationImpressionData
import com.chartboost.chartboostmediationsdk.ChartboostMediationSdk
import com.chartboost.chartboostmediationsdk.ad.ChartboostMediationBannerAdLoadRequest
import com.chartboost.chartboostmediationsdk.ad.ChartboostMediationBannerAdView
import com.chartboost.chartboostmediationsdk.ad.ChartboostMediationBannerAdView.ChartboostMediationBannerSize
import com.chartboost.chartboostmediationsdk.ad.ChartboostMediationBannerAdViewListener
import com.chartboost.chartboostmediationsdk.domain.ChartboostMediationError
import com.chartboost.chartboostmediationsdk.domain.Keywords
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BannerTestViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val keywordsRepository: KeywordsRepository,
        private val settingsPreferences: SettingsPreferences,
        private val adEventsLogger: AdEventsLogger,
        @IsDebugFlag private val isDebug: Boolean,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(BannerTestUiState())
        val uiState = _uiState.asStateFlow()

        private var keywords: Keywords = Keywords()

        val eventLog: StateFlow<List<String>> get() = adEventsLogger.eventLog

        private val locationName: String =
            if (isDebug) {
                // TODO HB-10138
                // Improvement for v1.1.0
                CB_BANNER_FOR_DEBUG
            } else {
                savedStateHandle.get<String>("locationName") ?: ""
            }

        private val chartboostMediationIlrdObserver =
            object : ChartboostMediationIlrdObserver {
                override fun onImpression(impData: ChartboostMediationImpressionData) {
                    if (impData.placementId == locationName) {
                        val methodName = createIlrdMethodName(ChartboostMediationIlrdObserver::onImpression.name)
                        updateCallbackChip(methodName, CallbackChipState.TRIGGERED)
                        Log.d(TAG, "Impression triggered for ILRD on $locationName. ${impData.ilrdInfo.toString(1)}")
                        adEventsLogger.logEvent("Impression triggered for ILRD on $locationName", methodName)
                    }
                }
            }

        init {
            Log.d(TAG, "init")

            if (locationName.isBlank()) {
                _uiState.update {
                    it.copy(
                        errorMessage = BannerScreenErrorMessage.NoLocationNameErrorMessage,
                        loadingButtonState = LoadingButtonState.DISABLED,
                        isClearBtnEnabled = false,
                    )
                }
            } else {
                _uiState.update { it.copy(adTitle = locationName) }
                viewModelScope.launch {
                    keywordsRepository.getKeywords().collect { map ->
                        _uiState.update { it.copy(numberOfKeywords = map.size) }
                        keywords =
                            Keywords().apply {
                                for (entry in map) {
                                    set(entry.key, entry.value)
                                }
                            }
                    }
                }
                initializeCallbackChips()

                ChartboostMediationSdk.subscribeIlrd(chartboostMediationIlrdObserver)
            }
        }

        private fun initializeCallbackChips() {
            val callbackChipMap = mutableMapOf<String, CallbackChipState>()

            ChartboostMediationIlrdObserver::class.java.declaredMethods.forEach { method ->
                callbackChipMap[createIlrdMethodName(method.name)] = CallbackChipState.NOT_TRIGGERED
            }

            ChartboostMediationBannerAdViewListener::class.java.declaredMethods.forEach { method ->
                callbackChipMap[method.name] = CallbackChipState.NOT_TRIGGERED
            }

            _uiState.update { it.copy(callbackChipMap = callbackChipMap) }
        }

        private fun updateCallbackChip(
            methodName: String,
            state: CallbackChipState,
        ) {
            _uiState.update { currentState ->
                currentState.copy(
                    callbackChipMap =
                        currentState.callbackChipMap.toMutableMap().apply {
                            put(methodName, state)
                        },
                )
            }
        }

        fun loadBanner(context: Context) {
            _uiState.update {
                it.copy(
                    loadingButtonState = LoadingButtonState.LOADING,
                    isClearBtnEnabled = true,
                    errorMessage = null,
                    callbackChipMap = getResetCallbackChipMap(it),
                )
            }
            val ad =
                ChartboostMediationBannerAdView(
                    context = context,
                    placement = locationName,
                    size = ChartboostMediationBannerSize.STANDARD,
                    chartboostMediationBannerAdViewListener =
                        object : ChartboostMediationBannerAdViewListener {
                            override fun onAdClicked(placement: String) {
                                val methodName = ChartboostMediationBannerAdViewListener::onAdClicked.name
                                Log.d(TAG, methodName)
                                updateCallbackChip(methodName, CallbackChipState.TRIGGERED)
                                adEventsLogger.logEvent(locationName, methodName)
                            }

                            override fun onAdImpressionRecorded(placement: String) {
                                val methodName = ChartboostMediationBannerAdViewListener::onAdImpressionRecorded.name
                                Log.d(TAG, methodName)
                                updateCallbackChip(methodName, CallbackChipState.TRIGGERED)
                                adEventsLogger.logEvent(locationName, methodName)
                            }

                            override fun onAdViewAdded(
                                placement: String,
                                child: View?,
                            ) {
                                val methodName = ChartboostMediationBannerAdViewListener::onAdViewAdded.name
                                Log.d(TAG, methodName)

                                if (child == null) {
                                    updateCallbackChip(methodName, CallbackChipState.ERROR)
                                    adEventsLogger.logEvent(locationName, "$methodName failed: child view is null")
                                } else {
                                    updateCallbackChip(methodName, CallbackChipState.TRIGGERED)
                                    adEventsLogger.logEvent(locationName, methodName)
                                }
                            }
                        },
                ).apply {
                    keywords = this@BannerTestViewModel.keywords
                }

            viewModelScope.launch(
                CoroutineExceptionHandler { _, throwable ->
                    _uiState.update {
                        it.copy(
                            banner = null,
                            loadingButtonState = LoadingButtonState.ENABLED,
                            isClearBtnEnabled = true,
                            errorMessage = BannerScreenErrorMessage.LoadFailedErrorMessage(ChartboostMediationError.LoadError.Exception),
                        )
                    }
                },
            ) {
                if (settingsPreferences.clearLogsOnLoad()) {
                    clearEventLogs()
                }
                val result =
                    ad.load(
                        ChartboostMediationBannerAdLoadRequest(
                            placement = locationName,
                            keywords = keywords,
                            size = ChartboostMediationBannerSize.STANDARD,
                        ),
                    )
                val error = result.error
                if (error == null) {
                    _uiState.update {
                        it.copy(
                            banner = ad,
                            loadingButtonState = LoadingButtonState.DISABLED,
                            isClearBtnEnabled = true,
                        )
                    }
                    adEventsLogger.logEvent("loadSucceeded")
                } else {
                    _uiState.update {
                        it.copy(
                            banner = null,
                            loadingButtonState = LoadingButtonState.ENABLED,
                            isClearBtnEnabled = true,
                            errorMessage = BannerScreenErrorMessage.LoadFailedErrorMessage(error),
                        )
                    }
                    Log.d(TAG, "Error loading ad: $error")
                    adEventsLogger.logEvent("loadFailed: $error")
                }
            }
        }

        /** Reset all callback chips to NOT_TRIGGERED */
        private fun getResetCallbackChipMap(uiState: BannerTestUiState) =
            uiState.callbackChipMap.mapValues { CallbackChipState.NOT_TRIGGERED }

        fun clearBanner() {
            _uiState.value.banner?.clearAd()
            adEventsLogger.logEvent("Banner cleared")
            _uiState.update {
                it.copy(
                    banner = null,
                    loadingButtonState = LoadingButtonState.ENABLED,
                    isClearBtnEnabled = false,
                    errorMessage = null,
                    callbackChipMap = getResetCallbackChipMap(it),
                )
            }
        }

        fun onBannerReleased() {
            _uiState.value.banner?.clearAd()
            _uiState.update { it.copy(banner = null) }
            adEventsLogger.logEvent("Banner view released")
        }

        private fun createIlrdMethodName(methodName: String): String = methodName.plus("Ilrd")

        override fun onCleared() {
            super.onCleared()
            // clear the keywords, to start fresh for another ad location
            keywordsRepository.clearKeywords()
            ChartboostMediationSdk.unsubscribeIlrd(chartboostMediationIlrdObserver)
        }

        fun clearEventLogs() {
            adEventsLogger.clear()
        }

        companion object {
            private val TAG = BannerTestViewModel::class.java.simpleName
            private const val CB_BANNER_FOR_DEBUG = "CBBanner"
        }
    }
