/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.error

/**
 * Base class for all Chartboost Core errors that can be used by either Core or its modules with the
 * exception of those in [ChartboostCoreError.CoreError].
 *
 * @property code The error code
 * @property message The error message
 * @property cause The error cause
 * @property resolution The error resolution
 */
sealed class ChartboostCoreError(
    override val code: Int,
    override val message: String,
    override val cause: String?,
    override val resolution: String,
) : ChartboostCoreErrorContract {
    /**
     * Group of errors related to initialization
     */
    sealed class InitializationError(
        code: Int,
        message: String,
        cause: String?,
        resolution: String,
    ) : ChartboostCoreError(code, message, cause, resolution) {
        object Unknown : InitializationError(
            code = 1000,
            message = "An unknown error occurred during initialization",
            cause = null,
            resolution = "Check the console for more information"
        )

        object InterfaceNotImplemented : InitializationError(
            code = 1001,
            message = "Module does not implement ChartboostCoreInitializableModule",
            cause = null,
            resolution = "Ensure the module implements ChartboostCoreInitializableModule"
        )

        object Exception : InitializationError(
            code = 1002,
            message = "An exception occurred during initialization",
            cause = null,
            resolution = "Check the exception details for more information"
        )

        object Timeout : InitializationError(
            code = 1003,
            message = "Initialization timed out",
            cause = "The module did not return a Result<Unit> within the specified timeout",
            resolution = "Ensure the module is calling Result.success(Unit) or Result.failure(Exception) within the specified timeout"
        )

        object ModuleAlreadyInitialized : InitializationError(
            code = 1004,
            message = "Module is already initialized",
            cause = null,
            resolution = "Ensure the module is not initialized more than once"
        )

        // TODO: Remove this error for production. It's only used to simulate a failure during testing.
        object SelfInducedFailure : InitializationError(
            code = 9999,
            message = "Module initialization failed because of a self-induced failure",
            cause = "This is not a real error. It is used to simulate a failure during initialization",
            resolution = "N/A"
        )
    }

    /**
     * Group of errors related to consent management
     */
    sealed class ConsentError(code: Int, message: String, cause: String?, resolution: String) :
        ChartboostCoreError(code, message, cause, resolution) {
        object Unknown : ConsentError(
            code = 2000,
            message = "An unknown error occurred during consent management",
            cause = null,
            resolution = "Check the console for more information"
        )

        object MissingInitializationParameters : ConsentError(
            code = 2001,
            message = "The consent management platform adapter was missing some parameters to initialize",
            cause = "Insufficient parameters passed to consent management platform adapter",
            resolution = "Check the dashboard or the initial parameters"
        )

        object InitializationError : ConsentError(
            code = 2002,
            message = "The consent management platform adapter failed to initialize",
            cause = "The underlying consent management platform failed to initialize",
            resolution = "Check the configuration of the consent management platform adapter"
        )

        object MissingAdapter : ConsentError(
            code = 2003,
            message = "The consent management platform's adapter is not available",
            cause = "The consent management platform failed to successfully initialize",
            resolution = "Please try to initialize this consent management platform adapter"
        )

        object DialogShowError : ConsentError(
            code = 2004,
            message = "The consent management platform failed to show a consent dialog",
            cause = "Something went wrong when showing a consent dialog. The consent management platform may not be configured correctly",
            resolution = "Please check your consent management platform adapter for missing initialization parameters"
        )

    }

    /**
     * Group of errors related to internal Chartboost Core operations
     */
    sealed class CoreError(code: Int, message: String, cause: String?, resolution: String) :
        ChartboostCoreError(code, message, cause, resolution) {
        object Unknown : CoreError(
            code = 3000,
            message = "An unknown error occurred during a Chartboost Core operation",
            cause = null,
            resolution = "Check the console for more information"
        )

        object ExponentialBackoffRetriesExhausted : CoreError(
            code = 3001,
            message = "Exponential backoff retries exhausted",
            cause = "The operation failed after the maximum number of retries",
            resolution = "Ensure the operation is not failing due to a transient error"
        )
    }
}
