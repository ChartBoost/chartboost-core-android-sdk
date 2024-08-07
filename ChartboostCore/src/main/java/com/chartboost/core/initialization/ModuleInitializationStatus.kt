/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.initialization

/**
 * Enumeration of the possible initialization statuses for a ChartboostCore module.
 */
enum class ModuleInitializationStatus {
    INITIALIZED,
    NOT_INITIALIZED,
    INITIALIZING,
}
