/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import android.content.Context
import android.content.SharedPreferences
import com.chartboost.core.consent.ConsentAdapter
import com.chartboost.core.consent.ConsentManagementPlatform
import com.chartboost.core.consent.ConsentObserver
import com.chartboost.core.environment.Environment
import com.chartboost.core.error.ChartboostCoreError
import com.chartboost.core.error.ChartboostCoreException
import com.chartboost.core.initialization.InitializableModule
import com.chartboost.core.initialization.InitializableModuleObserver
import com.chartboost.core.initialization.ModuleInitializationConfiguration
import com.chartboost.core.initialization.ModuleInitializationResult
import com.chartboost.core.initialization.ModuleInitializationStatus
import com.chartboost.core.initialization.SdkConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * @suppress
 *
 * Chartboost Core internal APIs. Refer to [ChartboostCore] for the public ones.
 */
internal object ChartboostCoreInternal {
    var prefs: SharedPreferences? = null

    /**
     * Map of module to its initialization status.
     */
    internal val moduleInitializationStatuses = ConcurrentHashMap<String,
            AtomicReference<ModuleInitializationStatus>>()

    internal val environment: Environment = Environment()

    internal val consent: ConsentManagementPlatform = ConsentManagementPlatform()
    private val coreModuleMarker = ChartboostCoreModule()

    /**
     * A set of predefined and essential modules to initialize in case the publisher does not provide any.
     */
    private val predefinedModules: Set<InitializableModule> = emptySet()

    internal suspend fun initializeSdk(
        context: Context,
        sdkConfiguration: SdkConfiguration,
        modules: List<InitializableModule>,
        observer: InitializableModuleObserver?,
    ) = coroutineScope {
        // Setting up the SharedPreferences for user-configurable settings.
        prefs = context.getSharedPreferences(
            "com.chartboost.core.canary",
            Context.MODE_PRIVATE
        )

        // Core fetches server config AND initializes the publisher-supplied set of modules concurrently.
        // Server config fetching should not block module initialization. Both actions are run via exponential backoff.

        val coreInitialization = async {
            environment.startSession(context)
            ResultManager.start(coreModuleMarker)

            ChartboostCoreLogger.d("Initializing ChartboostCore SDK with config: $sdkConfiguration")

            // TODO: Cache networking result for future use so that we don't have to fetch it again in case of subsequent Core SDK initializations in the same session.
            val result = Utils.executeWithExponentialBackoff {
                try {
                    ChartboostCoreLogger.d("Fetching initialization config from backend")
                    Utils.fetchInitializationDataFromBackend()
                    // TODO: Also initialize modules retrieved from backend.
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            // TODO: Send coreResultReport to backend for metrics.
            val resultReport = ResultManager.stop(
                coreModuleMarker,
                result.exceptionOrNull()?.let {
                    it as? ChartboostCoreException
                        ?: ChartboostCoreException(ChartboostCoreError.InitializationError.Exception)
                }
            )
        }

        val modulesInitialization = async(Dispatchers.Main) {
            modules.ifEmpty {
                ChartboostCoreLogger.w("No modules provided to initialization. Using predefined modules instead.")
                predefinedModules
            }.map { module ->
                async {
                    if (moduleInitializationStatuses[module.moduleId] == null) {
                        moduleInitializationStatuses[module.moduleId] = AtomicReference(
                            ModuleInitializationStatus.NOT_INITIALIZED
                        )
                    }

                    Utils.executeWithExponentialBackoff { retryCount ->
                        val result = initializeModule(context, module, sdkConfiguration, observer)

                        // Only notify the observer of the module initialization result if the module initialization has succeeded or if it's the last retry attempt.
                        // This is to prevent the observer from being notified of a module initialization failure multiple times in case of retries.
                        if (retryCount == Constants.MAX_RETRY_ATTEMPTS && !result.isSuccess) {
                            val moduleResult = ResultManager.stop(
                                module = module,
                                exception = result.exceptionOrNull()?.let {
                                    it as? ChartboostCoreException? ?: ChartboostCoreException(
                                        ChartboostCoreError.InitializationError.Exception
                                    )
                                }
                            )

                            Utils.safeExecute {
                                observer?.onModuleInitializationCompleted(moduleResult as ModuleInitializationResult)
                            }
                        }

                        result
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun initializeModule(
        context: Context,
        module: InitializableModule,
        sdkConfiguration: SdkConfiguration,
        observer: InitializableModuleObserver?,
    ): Result<Unit> {
        val moduleStatus = moduleInitializationStatuses[module.moduleId]?.get()
        val moduleInitializationConfiguration =
            createModuleInitializationConfiguration(sdkConfiguration)

        ChartboostCoreLogger.d("Initializing module ${module.moduleId} with config: $moduleInitializationConfiguration")
        ResultManager.start(module)

        if (moduleStatus == ModuleInitializationStatus.INITIALIZING) {
            ChartboostCoreLogger.d("Module ${module.moduleId} is currently being initialized. Skipping initialization.")
            // TODO: Returning either success or failure here is not ideal. We should return a result that indicates that the module is currently being initialized.
            return Result.success(Unit)
        }

        if (moduleStatus == ModuleInitializationStatus.INITIALIZED) {
            ChartboostCoreLogger.d("Module ${module.moduleId} is already initialized. Skipping initialization.")
            Utils.safeExecute {
                observer?.onModuleInitializationCompleted(
                    ResultManager.stop(
                        module = module,
                        exception = null
                    ) as ModuleInitializationResult
                )
            }
            return Result.success(Unit)
        }

        if (module is ConsentAdapter && consent.isAdapterAttached()) {
            ChartboostCoreLogger.d("Only one consent management platform is allowed to be initialized.")
            return Result.success(Unit)
        }

        moduleInitializationStatuses[module.moduleId]?.set(
            ModuleInitializationStatus.INITIALIZING
        )

        val result = try {
            module.initialize(
                context,
                moduleInitializationConfiguration,
                // TODO: Pipe actual config JSON from end users and/or backend here.
            )
        } catch (e: Exception) {
            ChartboostCoreLogger.e("Failed to initialize module ${module.moduleId} with exception: $e")
            Result.failure(e)
        }

        moduleInitializationStatuses[module.moduleId]?.set(
            if (result.isSuccess)
                ModuleInitializationStatus.INITIALIZED
            else
                ModuleInitializationStatus.NOT_INITIALIZED
        )

        // Only notify the observer if the module has been initialized.
        if (result.isSuccess) {
            val moduleResult = ResultManager.stop(
                module = module,
                exception = result.exceptionOrNull()?.let {
                    it as? ChartboostCoreException
                        ?: ChartboostCoreException(ChartboostCoreError.InitializationError.Exception)
                }
            )

            // If the module is also a consent observer, automatically add it as an observer for
            // consent changes.
            if (module is ConsentObserver) {
                consent.addObserver(module)
            }

            // If the adapter is a CMP, set it as the underlying CMP.
            if (module is ConsentAdapter) {
                consent.attachAdapter(module)
            }

            Utils.safeExecute {
                observer?.onModuleInitializationCompleted(moduleResult as ModuleInitializationResult)
            }
        }

        return result
    }

    /**
     * Creates a [ModuleInitializationConfiguration] object from the [SdkConfiguration] object.
     *
     * @param sdkConfiguration The [SdkConfiguration] object to use for creating the [ModuleInitializationConfiguration] object.
     */
    private fun createModuleInitializationConfiguration(sdkConfiguration: SdkConfiguration) =
        ModuleInitializationConfiguration(
            chartboostApplicationIdentifier = sdkConfiguration.chartboostApplicationIdentifier
        )
}

/**
 * @suppress
 */
internal class ChartboostCoreModule(
    override val moduleId: String = "ChartboostCoreModule",
    override val moduleVersion: String = Constants.SDK_VERSION,
) : InitializableModule {
    override fun updateProperties(configuration: JSONObject) {}

    override suspend fun initialize(
        context: Context,
        moduleInitializationConfiguration: ModuleInitializationConfiguration,
    ): Result<Unit> {
        return Result.success(Unit)
    }
}
