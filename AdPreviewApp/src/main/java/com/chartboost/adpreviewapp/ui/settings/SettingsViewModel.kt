/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chartboost.adpreviewapp.data.local.AuthCredentialsStore
import com.chartboost.adpreviewapp.di.MonetizationSdkVersion
import com.chartboost.adpreviewapp.domain.SettingsPreferences
import com.chartboost.adpreviewapp.domain.sdk.ChartboostSdkTestModeSwitcher
import com.chartboost.adpreviewapp.domain.system.LmtStatusChecker
import com.chartboost.adpreviewapp.domain.usecase.ClearAppsRepositoryUseCase
import com.chartboost.chartboostmediationsdk.ChartboostMediationSdk
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val preferences: SettingsPreferences,
        private val authCredentialsStore: AuthCredentialsStore,
        private val lmtStatusChecker: LmtStatusChecker,
        private val clearAppsRepositoryUseCase: ClearAppsRepositoryUseCase,
        private val testModeSwitcher: ChartboostSdkTestModeSwitcher,
        @MonetizationSdkVersion private val monetizationSdkVersion: String,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState

        init {
            loadSettings()
        }

        private fun loadSettings() {
            viewModelScope.launch {
                val lmtStatus = lmtStatusChecker.getStatus()
                val isTestMode = preferences.isTestMode()
                val clearLogsOnLoad = preferences.clearLogsOnLoad()
                _uiState.update {
                    it.copy(
                        isTestMode = isTestMode,
                        clearLogsOnLoad = clearLogsOnLoad,
                        lmtStatus = lmtStatus,
                        mediationSdkVersion = ChartboostMediationSdk.getVersion(),
                        monetizationSdkVersion = monetizationSdkVersion,
                    )
                }
            }
        }

        fun onTestModeToggled(enabled: Boolean) {
            testModeSwitcher.setTestMode(enabled)
                .onSuccess { handleTestSwitchSuccess(enabled) }
                .onFailure { handleTestSwitchFailure() }
        }

        private fun handleTestSwitchSuccess(enabled: Boolean) {
            viewModelScope.launch {
                preferences.setTestMode(enabled)
                _uiState.update { it.copy(isTestMode = enabled) }
            }
        }

        private fun handleTestSwitchFailure() {
            _uiState.update { it.copy(showTestModeErrorMessage = true) }
        }

        fun onClearLogsToggled(enabled: Boolean) {
            viewModelScope.launch {
                preferences.setClearLogsOnLoad(enabled)
                _uiState.update { it.copy(clearLogsOnLoad = enabled) }
            }
        }

        fun logout() {
            viewModelScope.launch {
                authCredentialsStore.clear()

                _uiState.update { it.copy(isLoggedOut = true) }
            }

            clearAppsRepositoryUseCase()
        }

        fun onLogoutHandled() {
            _uiState.update { it.copy(isLoggedOut = false) }
        }

        fun onToastShown() {
            _uiState.update { it.copy(showTestModeErrorMessage = false) }
        }

        private companion object {
            private const val TAG = "SettingsViewModel"
        }
    }
