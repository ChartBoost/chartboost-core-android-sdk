/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.error

/**
 * ChartboostCore custom Exception holder.
 *
 * @property error The underlying error.
 */
class ChartboostCoreException(val error: ChartboostCoreErrorContract) : Exception(error.message)
