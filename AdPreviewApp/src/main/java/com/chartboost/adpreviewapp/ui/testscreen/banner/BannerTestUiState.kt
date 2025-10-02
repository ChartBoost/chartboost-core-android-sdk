/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen.banner

import com.chartboost.adpreviewapp.ui.testscreen.CallbackChipState
import com.chartboost.adpreviewapp.ui.utils.model.LoadingButtonState
import com.chartboost.chartboostmediationsdk.ad.ChartboostMediationBannerAdView

data class BannerTestUiState(
    val adTitle: String? = null,
    val banner: ChartboostMediationBannerAdView? = null,
    val loadingButtonState: LoadingButtonState = LoadingButtonState.ENABLED,
    val isClearBtnEnabled: Boolean = false,
    val errorMessage: BannerScreenErrorMessage? = null,
    val callbackChipMap: Map<String, CallbackChipState> = emptyMap(),
    val numberOfKeywords: Int = 0,
)
