/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.initialization

import com.chartboost.core.ChartboostCoreResult
import com.chartboost.core.error.ChartboostCoreException

/**
 * Result of the Core SDK initialization.
 */
class SdkInitializationResult(
    override val start: Long,
    override val end: Long,
    override val duration: Long,
    override val exception: ChartboostCoreException?,
) : ChartboostCoreResult(start, end, duration, exception)
