/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen.interstitial

import com.chartboost.chartboostmediationsdk.domain.ChartboostMediationAdException
import com.chartboost.chartboostmediationsdk.domain.ChartboostMediationError

sealed class InterstitialScreenErrorMessage {
    data object NoLocationNameErrorMessage : InterstitialScreenErrorMessage()

    data class LoadFailedErrorMessage(val error: ChartboostMediationError) : InterstitialScreenErrorMessage()

    data class ShowFailedErrorMessage(val error: ChartboostMediationAdException) : InterstitialScreenErrorMessage()

    data object ShowFailedNoActivityErrorMessage : InterstitialScreenErrorMessage()
}
