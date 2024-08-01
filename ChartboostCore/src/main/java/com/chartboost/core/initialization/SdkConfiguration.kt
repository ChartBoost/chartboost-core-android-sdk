/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.initialization

/**
 * This data object holds initialization information for Chartboost Core SDK.
 */
data class SdkConfiguration(
    /**
     * The Chartboost application identifier. This is available on the Chartboost dashboard.
     */
    val chartboostApplicationIdentifier: String,
    /**
     * The list of publisher-specified modules to initialize. The modules will be initialized
     * simultaneously in the order specified. Only the first [ConsentAdapter] is initialized. If
     * other [ConsentAdapter]s are attempted to initialize, they will fail.
     */
    val modules: List<Module>,
    /**
     * Use this to skip modules. This will skip initialization for both the modules passed in to
     * the modules list and server-side modules with the same module identifier.
     */
    val skippedModuleIdentifiers: Set<String> = setOf(),
)
