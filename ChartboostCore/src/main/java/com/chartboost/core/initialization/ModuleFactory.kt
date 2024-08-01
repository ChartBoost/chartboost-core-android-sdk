/*
 * Copyright 2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.initialization

/**
 * If another platform such as Unity wants to create modules, then this is how they should do it.
 * Publishers should not extend one of these.
 */
@Suppress
interface ModuleFactory {
    suspend fun makeModule(className: String): Module?
}
