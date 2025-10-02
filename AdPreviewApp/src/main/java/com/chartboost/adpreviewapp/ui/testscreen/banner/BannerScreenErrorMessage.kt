/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen.banner

import com.chartboost.chartboostmediationsdk.domain.ChartboostMediationError

sealed class BannerScreenErrorMessage {
    data object NoLocationNameErrorMessage : BannerScreenErrorMessage()

    data class LoadFailedErrorMessage(val error: ChartboostMediationError) : BannerScreenErrorMessage()
}
