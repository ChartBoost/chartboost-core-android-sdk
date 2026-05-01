/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen.interstitial

import com.chartboost.adpreviewapp.ui.testscreen.CallbackChipState
import com.chartboost.adpreviewapp.ui.utils.model.LoadingButtonState

data class InterstitialTestUiState(
    val adTitle: String? = null,
    val loadingButtonState: LoadingButtonState = LoadingButtonState.ENABLED,
    val isShowBtnEnabled: Boolean = false,
    val isClearBtnEnabled: Boolean = false,
    val errorMessage: InterstitialScreenErrorMessage? = null,
    val callbackChipMap: Map<String, CallbackChipState> = emptyMap(),
    val numberOfKeywords: Int = 0,
)
