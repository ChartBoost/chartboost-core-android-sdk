/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen.interstitial

import android.app.Activity
import android.content.Context
import android.util.Log
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
import com.chartboost.chartboostmediationsdk.ad.ChartboostMediationFullscreenAd
import com.chartboost.chartboostmediationsdk.ad.ChartboostMediationFullscreenAdListener
import com.chartboost.chartboostmediationsdk.ad.ChartboostMediationFullscreenAdLoadRequest
import com.chartboost.chartboostmediationsdk.domain.ChartboostMediationAdException
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
class InterstitialTestViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val keywordsRepository: KeywordsRepository,
        private val settingsPreferences: SettingsPreferences,
        private val adEventsLogger: AdEventsLogger,
        @IsDebugFlag private val isDebug: Boolean,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(InterstitialTestUiState())
        val uiState = _uiState.asStateFlow()

        private var keywords: Keywords = Keywords()
        private var loadedAd: ChartboostMediationFullscreenAd? = null

        val eventLog: StateFlow<List<String>> get() = adEventsLogger.eventLog

        private val locationName: String =
            if (isDebug) {
                CB_INTERSTITIAL_FOR_DEBUG
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
                        errorMessage = InterstitialScreenErrorMessage.NoLocationNameErrorMessage,
                        loadingButtonState = LoadingButtonState.DISABLED,
                        isShowBtnEnabled = false,
                        isClearBtnEnabled = false,
                    )
                }
                adEventsLogger.logEvent(locationName, "Error: locationName is blank")
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

            ChartboostMediationFullscreenAdListener::class.java.declaredMethods.forEach { method ->
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

        fun loadFullscreenAd(context: Context) {
            loadedAd?.invalidate()
            _uiState.update {
                it.copy(
                    loadingButtonState = LoadingButtonState.LOADING,
                    isShowBtnEnabled = false,
                    isClearBtnEnabled = true,
                    errorMessage = null,
                    callbackChipMap = getResetCallbackChipMap(it),
                )
            }

            val request =
                ChartboostMediationFullscreenAdLoadRequest(
                    placement = locationName,
                    keywords = keywords,
                )

            viewModelScope.launch(
                CoroutineExceptionHandler { _, throwable ->
                    _uiState.update {
                        it.copy(
                            loadingButtonState = LoadingButtonState.ENABLED,
                            isShowBtnEnabled = false,
                            isClearBtnEnabled = true,
                            errorMessage =
                                InterstitialScreenErrorMessage.LoadFailedErrorMessage(
                                    ChartboostMediationError.LoadError.Exception,
                                ),
                        )
                    }
                    Log.d(TAG, "Error loading ad: $throwable")
                    adEventsLogger.logEvent(locationName, "loadFailed: $throwable")
                },
            ) {
                if (settingsPreferences.clearLogsOnLoad()) {
                    clearEventLogs()
                }
                val result =
                    ChartboostMediationFullscreenAd.loadFullscreenAd(
                        context,
                        request,
                        object : ChartboostMediationFullscreenAdListener {
                            override fun onAdClicked(ad: ChartboostMediationFullscreenAd) {
                                val methodName = ChartboostMediationFullscreenAdListener::onAdClicked.name
                                Log.d(TAG, methodName)
                                updateCallbackChip(methodName, CallbackChipState.TRIGGERED)
                                adEventsLogger.logEvent(locationName, methodName)
                            }

                            override fun onAdClosed(
                                ad: ChartboostMediationFullscreenAd,
                                error: ChartboostMediationAdException?,
                            ) {
                                val methodName = ChartboostMediationFullscreenAdListener::onAdClosed.name
                                Log.d(TAG, methodName)
                                updateCallbackChip(methodName, if (error == null) CallbackChipState.TRIGGERED else CallbackChipState.ERROR)
                                adEventsLogger.logEvent(locationName, methodName)

                                // After ad is closed, enable load button again and disable show button
                                _uiState.update {
                                    it.copy(
                                        loadingButtonState = LoadingButtonState.ENABLED,
                                        isShowBtnEnabled = false,
                                    )
                                }
                                ad.invalidate()
                                nullifyIfLastLoaded(ad)
                            }

                            override fun onAdRewarded(ad: ChartboostMediationFullscreenAd) {
                                val methodName = ChartboostMediationFullscreenAdListener::onAdRewarded.name
                                Log.d(TAG, methodName)
                                updateCallbackChip(methodName, CallbackChipState.TRIGGERED)
                                adEventsLogger.logEvent(locationName, methodName)
                            }

                            override fun onAdExpired(ad: ChartboostMediationFullscreenAd) {
                                val methodName = ChartboostMediationFullscreenAdListener::onAdExpired.name
                                Log.d(TAG, methodName)
                                updateCallbackChip(methodName, CallbackChipState.TRIGGERED)
                                adEventsLogger.logEvent(locationName, methodName)

                                // Ad expired, need to load again
                                _uiState.update {
                                    it.copy(
                                        loadingButtonState = LoadingButtonState.ENABLED,
                                        isShowBtnEnabled = false,
                                    )
                                }
                                ad.invalidate()
                                nullifyIfLastLoaded(ad)
                            }

                            override fun onAdImpressionRecorded(ad: ChartboostMediationFullscreenAd) {
                                val methodName = ChartboostMediationFullscreenAdListener::onAdImpressionRecorded.name
                                Log.d(TAG, methodName)
                                updateCallbackChip(methodName, CallbackChipState.TRIGGERED)
                                adEventsLogger.logEvent(locationName, methodName)
                            }
                        },
                    )

                val error = result.error
                if (error == null) {
                    loadedAd = result.ad
                    _uiState.update {
                        it.copy(
                            loadingButtonState = LoadingButtonState.ENABLED,
                            isShowBtnEnabled = true,
                            isClearBtnEnabled = true,
                        )
                    }
                    adEventsLogger.logEvent(locationName, "loadSucceeded")
                } else {
                    _uiState.update {
                        it.copy(
                            loadingButtonState = LoadingButtonState.ENABLED,
                            isShowBtnEnabled = false,
                            isClearBtnEnabled = true,
                            errorMessage = InterstitialScreenErrorMessage.LoadFailedErrorMessage(error),
                        )
                    }
                    Log.d(TAG, "Error loading ad: $error")
                    adEventsLogger.logEvent(locationName, "loadFailed: $error")
                }
            }
        }

        private fun nullifyIfLastLoaded(ad: ChartboostMediationFullscreenAd) {
            Log.d(TAG, "invalidateIfLastLoaded")
            if (loadedAd?.loadId == ad.loadId) {
                loadedAd = null
            }
        }

        private fun invalidateAndNullifyLoadedAd() {
            loadedAd?.invalidate()
            loadedAd = null
        }

        fun showFullscreenAd(activity: Activity?) {
            loadedAd?.let { ad ->

                if (activity == null) {
                    _uiState.update {
                        it.copy(
                            isShowBtnEnabled = false,
                            errorMessage = InterstitialScreenErrorMessage.ShowFailedNoActivityErrorMessage,
                        )
                    }
                    adEventsLogger.logEvent(locationName, "showFailed: Activity is null")
                } else {
                    viewModelScope.launch(
                        CoroutineExceptionHandler { _, throwable ->
                            _uiState.update {
                                it.copy(
                                    isShowBtnEnabled = false,
                                    errorMessage =
                                        InterstitialScreenErrorMessage.ShowFailedErrorMessage(
                                            ChartboostMediationAdException(
                                                ChartboostMediationError.ShowError.Exception,
                                            ),
                                        ),
                                )
                            }
                            Log.d(TAG, "Error showing ad: $throwable")
                            adEventsLogger.logEvent(locationName, "showFailed: $throwable")
                        },
                    ) {
                        _uiState.update {
                            it.copy(
                                isShowBtnEnabled = false,
                                errorMessage = null,
                            )
                        }
                        ad.show(activity)
                    }
                }
            }
        }

        fun clearAd() {
            invalidateAndNullifyLoadedAd()
            adEventsLogger.logEvent(locationName, "Ad cleared")
            _uiState.update {
                it.copy(
                    loadingButtonState = LoadingButtonState.ENABLED,
                    isShowBtnEnabled = false,
                    isClearBtnEnabled = false,
                    errorMessage = null,
                    callbackChipMap = getResetCallbackChipMap(it),
                )
            }
        }

        /** Reset all callback chips to NOT_TRIGGERED */
        private fun getResetCallbackChipMap(uiState: InterstitialTestUiState) =
            uiState.callbackChipMap.mapValues { CallbackChipState.NOT_TRIGGERED }

        private fun createIlrdMethodName(methodName: String): String = methodName.plus("Ilrd")

        override fun onCleared() {
            super.onCleared()
            invalidateAndNullifyLoadedAd()
            keywordsRepository.clearKeywords()
            ChartboostMediationSdk.unsubscribeIlrd(chartboostMediationIlrdObserver)
        }

        fun clearEventLogs() {
            adEventsLogger.clear()
        }

        companion object {
            private val TAG = InterstitialTestViewModel::class.java.simpleName
            private const val CB_INTERSTITIAL_FOR_DEBUG = "CBInterstitial"
        }
    }
