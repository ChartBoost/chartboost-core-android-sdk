/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.locations

import com.chartboost.adpreviewapp.data.model.AdLocation
import com.chartboost.adpreviewapp.data.model.AdLocationsErrorMessage

data class AdLocationsUiState(
    val nameToDisplay: String = "",
    val iconUrl: String = "",
    val filteredLocations: List<AdLocation> = emptyList(),
    val query: String = "",
    val errorMessage: AdLocationsErrorMessage? = null,
    val showForceCloseDialog: Boolean = false,
)
