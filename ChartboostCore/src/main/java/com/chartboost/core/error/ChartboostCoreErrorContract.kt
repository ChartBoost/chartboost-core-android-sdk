/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.error

/**
 * Represents an error that can be thrown by the Chartboost Core SDK and/or its modules.
 *
 * In addition to the already defined errors ([ChartboostCoreError]), modules or other parts of the
 * SDK can define their own errors by extending this interface. However, it is recommended to use
 * [ChartboostCoreError] whenever possible to ensure consistency across the SDK and avoid ambiguity.
 *
 * @property code The error code as a four digit integer, e.g. 1000
 * @property message The error message, e.g. "Initialization timed out"
 * @property cause The error cause, e.g. "The module did not return a Result<Unit> within the specified timeout"
 * @property resolution The error resolution, e.g. "Ensure the module is calling Result.success(Unit) or Result.failure(Exception) within the specified timeout"
 */
interface ChartboostCoreErrorContract {
    val code: Int
    val message: String
    val cause: String?
    val resolution: String
}
