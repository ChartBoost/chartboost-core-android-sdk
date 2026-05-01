/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.apps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chartboost.adpreviewapp.di.IsDebugFlag
import com.chartboost.adpreviewapp.domain.sdk.ChartboostSdkInitializer
import com.chartboost.adpreviewapp.domain.usecase.GetAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AppsListViewModel
    @Inject
    constructor(
        private val getAppsUseCase: GetAppsUseCase,
        private val chartboostSdkInitializer: ChartboostSdkInitializer,
        @IsDebugFlag private val isDebug: Boolean,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AppsUiState())
        val uiState: StateFlow<AppsUiState> = _uiState

        init {
            fetchApps()
        }

        private fun fetchApps() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                try {
                    val apps = getAppsUseCase()
                    _uiState.update {
                        it.copy(
                            apps = apps,
                            filteredApps = apps,
                            isLoading = false,
                        )
                    }
                } catch (e: Exception) {
                    val message = if (e is HttpException && e.code() == 401) "auth_error" else e.message ?: "Unknown error"
                    _uiState.update { it.copy(error = message, isLoading = false) }
                }
            }
        }

        fun onAppSelected(appId: String) {
            _uiState.update { it.copy(isSdkInitializing = true) }

            viewModelScope.launch(
                CoroutineExceptionHandler { _, throwable ->
                    Log.e(TAG, "Error while initializing Chartboost SDK", throwable)
                    _uiState.update {
                        it.copy(
                            sdkInitResult = SdkInitResult.Failure(throwable.message ?: throwable.stackTraceToString()),
                            isSdkInitializing = false,
                        )
                    }
                },
            ) {
                val result = initializeSdkWithAppId(appId)
                _uiState.update { it.copy(sdkInitResult = result, isSdkInitializing = false) }
            }
        }

        private suspend fun initializeSdkWithAppId(appId: String): SdkInitResult {
            val appIdToInitialize = if (!isDebug) appId else CANARY_APP_ID_FOR_DEBUG
            return chartboostSdkInitializer.initialize(appIdToInitialize).fold(
                onSuccess = {
                    Log.d(TAG, "SDK successfully initialized for appId=$appId")
                    SdkInitResult.Success(appId)
                },
                onFailure = {
                    Log.e(TAG, "SDK initialization failed", it)
                    SdkInitResult.Failure(it.message ?: "SDK init error")
                },
            )
        }

        fun onSdkInitHandled() {
            _uiState.update { it.copy(sdkInitResult = null) }
        }

        fun onQueryChanged(query: String) {
            val filtered =
                _uiState.value.apps.filter { app ->
                    listOfNotNull(app.nickname, app.name, app.storeAppId)
                        .any { it.contains(query, ignoreCase = true) }
                }
            _uiState.update { it.copy(query = query, filteredApps = filtered) }
        }

        companion object {
            private const val TAG = "AppsListViewModel"
            private const val CANARY_APP_ID_FOR_DEBUG = "5a4e797538a5f00cf60738d6"
        }
    }
