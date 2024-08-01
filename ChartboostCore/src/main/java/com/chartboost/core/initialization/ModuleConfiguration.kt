/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.initialization

/**
 * Configuration object for initializing Chartboost Core modules.
 *
 * @property chartboostApplicationIdentifier The Chartboost application identifier.
 */
data class ModuleConfiguration(val chartboostApplicationIdentifier: String)
