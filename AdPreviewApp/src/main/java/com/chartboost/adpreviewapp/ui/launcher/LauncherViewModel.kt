/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chartboost.adpreviewapp.domain.usecase.CheckIfLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel
    @Inject
    constructor(
        private val checkIfLoggedInUseCase: CheckIfLoggedInUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow<LauncherState>(LauncherState.Checking)
        val state: StateFlow<LauncherState> = _state

        init {
            viewModelScope.launch {
                val isLoggedIn = checkIfLoggedInUseCase()
                _state.value = if (isLoggedIn) LauncherState.LoggedIn else LauncherState.LoggedOut
            }
        }
    }
