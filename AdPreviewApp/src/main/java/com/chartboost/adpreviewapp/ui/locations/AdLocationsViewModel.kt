/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.locations

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chartboost.adpreviewapp.data.model.AdLocation
import com.chartboost.adpreviewapp.data.model.AdLocationsErrorMessage
import com.chartboost.adpreviewapp.data.model.containsQuery
import com.chartboost.adpreviewapp.domain.usecase.GetAdLocationsUseCase
import com.chartboost.adpreviewapp.domain.usecase.GetAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdLocationsViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getAdLocations: GetAdLocationsUseCase,
        private val getAcceptedAppsUseCase: GetAppsUseCase,
    ) : ViewModel() {
        private var adLocations = emptyList<AdLocation>()

        private val _uiState = MutableStateFlow(AdLocationsUiState())
        val uiState = _uiState.asStateFlow()

        init {
            val appId = savedStateHandle.get<String>("appId")
            if (appId == null) {
                _uiState.update { it.copy(errorMessage = AdLocationsErrorMessage.NO_APP_ID) }
            } else {
                viewModelScope.launch {
                    val appInfoDeferred = async { getAppInfo(appId) }
                    val locationsDeferred =
                        async {
                            runCatching { getAdLocations(appId) }
                        }

                    val appInfo = appInfoDeferred.await()
                    val locationsResult = locationsDeferred.await()

                    locationsResult.onSuccess { locations ->
                        adLocations = locations
                        val errorMessage =
                            if (locations.isEmpty()) {
                                AdLocationsErrorMessage.NO_LOCATIONS
                            } else {
                                null
                            }
                        _uiState.update {
                            it.copy(
                                filteredLocations = locations,
                                nameToDisplay = appInfo.nameToDisplay,
                                iconUrl = appInfo.iconUrl,
                                errorMessage = errorMessage,
                            )
                        }
                    }.onFailure { exception ->
                        Log.e(TAG, exception.message ?: "Getting locations for Ad : $appId exception!")
                        _uiState.update {
                            it.copy(
                                filteredLocations = emptyList(),
                                nameToDisplay = appInfo.nameToDisplay,
                                iconUrl = appInfo.iconUrl,
                                errorMessage = AdLocationsErrorMessage.ERROR_FETCHING_LOCATIONS,
                            )
                        }
                    }
                }
            }
        }

        fun onQueryChanged(query: String) {
            val filtered =
                adLocations.filter {
                    it.containsQuery(query)
                }
            _uiState.update { it.copy(query = query, filteredLocations = filtered) }
        }

        // get app info: name, platform, iconUrl
        private suspend fun getAppInfo(appId: String): AdLocationsUiState {
            return runCatching {
                getAcceptedAppsUseCase().find { it.id == appId }
            }.fold(
                onSuccess = { app ->
                    AdLocationsUiState(
                        nameToDisplay = app?.nickname ?: app?.name ?: "",
                        iconUrl = app?.icon.orEmpty(),
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, exception.message ?: "Error getting info for App: $appId")
                    AdLocationsUiState(nameToDisplay = "")
                },
            )
        }

        fun onBackRequested() {
            _uiState.update { it.copy(showForceCloseDialog = true) }
        }

        fun dismissForceCloseDialog() {
            _uiState.update { it.copy(showForceCloseDialog = false) }
        }

        private companion object {
            private val TAG = AdLocationsViewModel::class.java.simpleName
        }
    }
