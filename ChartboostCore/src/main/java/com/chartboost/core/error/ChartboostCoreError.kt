/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.error

import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

/**
 * Base class for all Chartboost Core errors that can be used by either Core or its modules with the
 * exception of those in [ChartboostCoreError.CoreError].
 *
 * @property code The error code
 * @property message The error message
 * @property cause The error cause
 * @property resolution The error resolution
 */
@Serializable
sealed class ChartboostCoreError(
    override val code: Int,
    override val message: String,
    override val cause: String?,
    override val resolution: String,
) : ChartboostCoreErrorContract {
    /**
     * Group of errors related to initialization
     */
    @Serializable
    sealed class InitializationError(
        @SerialName("coreInitError_code") override val code: Int,
        @SerialName("coreInitError_message") override val message: String,
        @SerialName("coreInitError_cause") override val cause: String?,
        @SerialName("coreInitError_resolution") override val resolution: String,
    ) : ChartboostCoreError(code, message, cause, resolution) {
        @Serializable
        object Unknown : InitializationError(
            code = 1000,
            message = "An unknown error occurred during initialization",
            cause = null,
            resolution = "Check the console for more information",
        )

        @Serializable
        object InterfaceNotImplemented : InitializationError(
            code = 1001,
            message = "Module does not implement ChartboostCoreInitializableModule",
            cause = null,
            resolution = "Ensure the module implements ChartboostCoreInitializableModule",
        )

        @Serializable
        object Exception : InitializationError(
            code = 1002,
            message = "An exception occurred during initialization",
            cause = null,
            resolution = "Check the exception details for more information",
        )

        @Serializable
        object Timeout : InitializationError(
            code = 1003,
            message = "Initialization timed out",
            cause = "The module did not return a Result<Unit> within the specified timeout",
            resolution = "Ensure the module is calling Result.success(Unit) or Result.failure(Exception) within the specified timeout",
        )

        @Serializable
        object ModuleAlreadyInitialized : InitializationError(
            code = 1004,
            message = "Module is already initialized",
            cause = null,
            resolution = "Ensure the module is not initialized more than once",
        )

        @Serializable
        object ActivityRequired : InitializationError(
            code = 1005,
            message = "Activity is required.",
            cause = "A Context that is not an Activity was sent during initialization.",
            resolution = "Ensure that an Activity is passed in for initialization.",
        )

        @Serializable
        object MultipleConsentAdapters : InitializationError(
            code = 1006,
            message = "Multiple ConsentAdapters are provided, but only the first one is accepted.",
            cause = "Only one ConsentAdapter is allowed to initialize.",
            resolution = "Please specify only one ConsentAdapter for initialization.",
        )

        @Serializable
        object ConsentAdapterPreviouslyInitialized : InitializationError(
            code = 1007,
            message = "A ConsentAdapter has already successfully initialized. Ignoring this.",
            cause = "Attempting to initialize a ConsentAdapter when one has already been initialized.",
            resolution = "Do not initialize more than one ConsentAdapter.",
        )
    }

    /**
     * Group of errors related to consent management
     */
    @Serializable
    sealed class ConsentError(
        @SerialName("coreConsentError_code") override val code: Int,
        @SerialName("coreConsentError_message") override val message: String,
        @SerialName("coreConsentError_cause") override val cause: String?,
        @SerialName("coreConsentError_resolution") override val resolution: String,
    ) :
        ChartboostCoreError(code, message, cause, resolution) {
        @Serializable
        object Unknown : ConsentError(
            code = 2000,
            message = "An unknown error occurred during consent management",
            cause = null,
            resolution = "Check the console for more information",
        )

        @Serializable
        object MissingInitializationParameters : ConsentError(
            code = 2001,
            message = "The consent management platform adapter was missing some parameters to initialize",
            cause = "Insufficient parameters passed to consent management platform adapter",
            resolution = "Check the dashboard or the initial parameters",
        )

        @Serializable
        object InitializationError : ConsentError(
            code = 2002,
            message = "The consent management platform adapter failed to initialize",
            cause = "The underlying consent management platform failed to initialize",
            resolution = "Check the configuration of the consent management platform adapter",
        )

        @Serializable
        object MissingAdapter : ConsentError(
            code = 2003,
            message = "The consent management platform's adapter is not available",
            cause = "The consent management platform failed to successfully initialize",
            resolution = "Please try to initialize this consent management platform adapter",
        )

        @Serializable
        object DialogShowError : ConsentError(
            code = 2004,
            message = "The consent management platform failed to show a consent dialog",
            cause = "Something went wrong when showing a consent dialog. The consent management platform may not be configured correctly",
            resolution = "Please check your consent management platform adapter for missing initialization parameters",
        )

        @Serializable
        object ActionNotAllowed : ConsentError(
            code = 2005,
            message = "This action is not allowed by the underlying consent management platform.",
            cause = "This consent management platform does not allow this action to occur.",
            resolution = "Do not call this method for this consent management platform.",
        )
    }

    /**
     * Group of errors related to internal Chartboost Core operations
     */
    @Serializable
    sealed class CoreError(
        @SerialName("coreError_code") override val code: Int,
        @SerialName("coreError_message") override val message: String,
        @SerialName("coreError_cause") override val cause: String?,
        @SerialName("coreError_resolution") override val resolution: String,
    ) :
        ChartboostCoreError(code, message, cause, resolution) {
        @Serializable
        object Unknown : CoreError(
            code = 3000,
            message = "An unknown error occurred during a Chartboost Core operation",
            cause = null,
            resolution = "Check the console for more information",
        )

        @Serializable
        object ExponentialBackoffRetriesExhausted : CoreError(
            code = 3001,
            message = "Exponential backoff retries exhausted",
            cause = "The operation failed after the maximum number of retries",
            resolution = "Ensure the operation is not failing due to a transient error",
        )

        @Serializable
        object SerializationError : CoreError(
            code = 3003,
            message = "Something went wrong during serialization",
            cause = "A malformed JSON caused a problem during serialization or deserialization",
            resolution = "This error usually means there's an issue with Core itself or the server. Please contact support",
        )

        @Serializable
        object UnknownNetworkingError : CoreError(
            code = 3003,
            message = "Something went wrong with a network request",
            cause = "An unknown networking problem has occurred",
            resolution = "Try again later",
        )

        @Serializable
        object NoConnectivityError : CoreError(
            code = 3004,
            message = "Unable to reach the server",
            cause = "There is no internet available",
            resolution = "Try again after connecting to the internet",
        )
    }
}

/**
 * The [SerializersModule] for the [ChartboostCoreError] sealed classes.
 */
val coreErrorSerializersModule =
    SerializersModule {
        polymorphic(ChartboostCoreError::class) {
            registerAllSubclasses<ChartboostCoreError>()
        }
    }

/**
 * Automate the process of registering all the error subclasses of each sealed class with their
 * serializers.
 *
 * @param T The sealed class to register
 * @receiver The [SerializersModuleBuilder] to register the serializers with.
 */
@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> SerializersModuleBuilder.registerAllSubclasses() {
    val kClass = T::class
    val subClasses = kClass.sealedSubclasses

    for (subClass in subClasses) {
        val serializer = subClass.serializer()

        @Suppress("UNCHECKED_CAST")
        (this::class.members.find { it.name == "subclass" } as? (KClass<*>, KSerializer<*>) -> Unit)
            ?.invoke(subClass, serializer)
    }
}
