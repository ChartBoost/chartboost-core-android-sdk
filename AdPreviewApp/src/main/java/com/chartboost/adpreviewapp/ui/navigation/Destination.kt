/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LauncherDestination

@Serializable
data object LoginDestination

@Serializable
data object AppsDestination

@Serializable
data class AdLocationsDestination(val appId: String)

@Serializable
data object SettingsDestination

@Serializable
data class AdLocationTestDestination(val locationName: String, val adType: String)

@Serializable
data object KeywordsDestination
