/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import android.content.Context
import android.content.SharedPreferences
import com.chartboost.core.Utils.toJSONObject
import com.chartboost.core.consent.ConsentAdapter
import com.chartboost.core.consent.ConsentManagementPlatform
import com.chartboost.core.consent.ConsentObserver
import com.chartboost.core.environment.Environment
import com.chartboost.core.environment.EnvironmentObserver
import com.chartboost.core.error.ChartboostCoreError
import com.chartboost.core.error.ChartboostCoreException
import com.chartboost.core.initialization.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.full.createInstance

/**
 * @suppress
 *
 * Chartboost Core internal APIs. Refer to [ChartboostCore] for the public ones.
 */
internal object ChartboostCoreInternal {
    private var prefs: SharedPreferences? = null

    /**
     * Map of module to its initialization status.
     */
    internal val moduleInitializationStatuses =
        ConcurrentHashMap<
            String,
            AtomicReference<ModuleInitializationStatus>,
        >()

    /**
     * We actually just keep one actual Environment class that conforms to all the Environments.
     */
    internal val environment: Environment = Environment()

    /**
     * This is where we keep the consent management platform.
     */
    internal val consent: ConsentManagementPlatform = ConsentManagementPlatform()

    /**
     * The controller that handles the initialization of Core.
     */
    private val initConfigController = InitConfigController()

    /**
     * We use this to build metrics.
     */
    private val coreModuleMarker = ChartboostCoreModule()

    internal suspend fun initializeSdk(
        context: Context,
        sdkConfiguration: SdkConfiguration,
        observer: ModuleObserver?,
    ) = coroutineScope {
        // Setting up the SharedPreferences for user-configurable settings.
        prefs =
            context.getSharedPreferences(
                "com.chartboost.core.canary",
                Context.MODE_PRIVATE,
            )

        environment.startSession(context)

        // Core fetches server config AND initializes the publisher-supplied set of modules concurrently.
        // Server config fetching should not block module initialization. Both actions are run via exponential backoff.

        val coreInitialization =
            async {
                ResultManager.start(coreModuleMarker)

                ChartboostCoreLogger.d("Initializing ChartboostCore SDK with config: $sdkConfiguration")
                var lastException: Exception? = null
                try {
                    ChartboostCoreLogger.d("Fetching initialization config from backend")
                    val config = initConfigController.fetchConfig(context.applicationContext)
                    val serverModuleList =
                        config?.modules
                            ?: throw ChartboostCoreException(ChartboostCoreError.InitializationError.Timeout)
                    val logLevelServer = config.logLevel
                    logLevelServer?.let {
                        ChartboostCoreLogger.serverLogLevelOverride = ChartboostCoreLogLevel.fromInt(it)
                    } ?: run {
                        // TODO: Use client-side (Canary-defined) log level
                    }

                    val modules = mutableListOf<Module>()
                    serverModuleList.forEach {
                        try {
                            val jsonConfig = it.jsonConfig?.toJSONObject() ?: JSONObject()
                            val module =
                                if (it.className.isNotBlank()) {
                                    Class.forName(it.className).kotlin.createInstance() as Module
                                } else {
                                    ChartboostCore.nonNativeModuleFactory?.makeModule(it.nonNativeClassName)
                                        ?: run {
                                            ChartboostCoreLogger.w(
                                                "moduleFactory is null or makeModule failed so unable to create non-native class ${it.nonNativeClassName}",
                                            )
                                            return@forEach
                                        }
                                }

                            module.updateCredentials(context, jsonConfig)
                            modules.add(module)
                        } catch (e: Exception) {
                            // Anything can happen here as we are instantiating an arbitrary
                            // class from a class name, so we're being extra careful. We
                            // ignore any classes we cannot find.
                            ChartboostCoreLogger.d("Unable to create module: ${it.className}")
                        }
                    }
                    initializeModules(context, modules, sdkConfiguration, observer)
                } catch (e: Exception) {
                    lastException = e
                }

                // TODO: Send coreResultReport to backend for metrics.
                val resultReport =
                    ResultManager.stop(
                        coreModuleMarker,
                        lastException?.let {
                            it as? ChartboostCoreException
                                ?: ChartboostCoreException(ChartboostCoreError.InitializationError.Exception)
                        },
                    )
            }

        val filteredModules = sdkConfiguration.modules.filterNot { it is ConsentAdapter }.toMutableList()
        val consentAdapters = sdkConfiguration.modules.filter { it is ConsentAdapter }.toMutableList()
        consentAdapters.removeFirstOrNull()?.let {
            if (consent.isAdapterAttached()) {
                Utils.safeExecute {
                    observer?.onModuleInitializationCompleted(
                        ModuleInitializationResult(
                            start = System.currentTimeMillis(),
                            end = System.currentTimeMillis(),
                            duration = 0L,
                            exception =
                                ChartboostCoreException(
                                    ChartboostCoreError.InitializationError.ConsentAdapterPreviouslyInitialized,
                                ),
                            moduleId = it.moduleId,
                            moduleVersion = it.moduleVersion,
                        ),
                    )
                }
                consentAdapters.add(0, it)
                return@let
            }
            filteredModules.add(it)
        }
        consentAdapters.forEach {
            Utils.safeExecute {
                observer?.onModuleInitializationCompleted(
                    ModuleInitializationResult(
                        start = System.currentTimeMillis(),
                        end = System.currentTimeMillis(),
                        duration = 0L,
                        exception = ChartboostCoreException(ChartboostCoreError.InitializationError.MultipleConsentAdapters),
                        moduleId = it.moduleId,
                        moduleVersion = it.moduleVersion,
                    ),
                )
            }
        }

        initializeModules(context, filteredModules, sdkConfiguration, observer)
    }

    private suspend fun initializeModules(
        context: Context,
        modules: List<Module>,
        sdkConfiguration: SdkConfiguration,
        observer: ModuleObserver?,
    ) {
        withContext(Dispatchers.Main) {
            modules.map { module ->
                async singleModuleInit@{
                    if (moduleInitializationStatuses[module.moduleId] == null) {
                        moduleInitializationStatuses[module.moduleId] =
                            AtomicReference(
                                ModuleInitializationStatus.NOT_INITIALIZED,
                            )
                    }
                    if (sdkConfiguration.skippedModuleIdentifiers.contains(module.moduleId)) {
                        ChartboostCoreLogger.d("Publisher instructed to skip module ${module.moduleId}")
                        return@singleModuleInit
                    }

                    Utils.executeWithExponentialBackoff { retryCount ->
                        val result = initializeModule(context, module, sdkConfiguration, observer)

                        // Only notify the observer of the module initialization result if the module initialization has succeeded or if it's the last retry attempt.
                        // This is to prevent the observer from being notified of a module initialization failure multiple times in case of retries.
                        if (retryCount == Constants.MAX_RETRY_ATTEMPTS && !result.isSuccess) {
                            val moduleResult =
                                ResultManager.stop(
                                    module = module,
                                    exception =
                                        result.exceptionOrNull()?.let {
                                            it as? ChartboostCoreException? ?: ChartboostCoreException(
                                                ChartboostCoreError.InitializationError.Exception,
                                            )
                                        },
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
        module: Module,
        sdkConfiguration: SdkConfiguration,
        observer: ModuleObserver?,
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
                        exception = null,
                    ) as ModuleInitializationResult,
                )
            }
            return Result.success(Unit)
        }

        if (module is ConsentAdapter && consent.isAdapterAttached()) {
            ChartboostCoreLogger.d("Only one consent management platform is allowed to be initialized.")
            return Result.success(Unit)
        }

        moduleInitializationStatuses[module.moduleId]?.set(
            ModuleInitializationStatus.INITIALIZING,
        )

        val result =
            try {
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
            if (result.isSuccess) {
                ModuleInitializationStatus.INITIALIZED
            } else {
                ModuleInitializationStatus.NOT_INITIALIZED
            },
        )

        // Only notify the observer if the module has been initialized.
        if (result.isSuccess) {
            val moduleResult =
                ResultManager.stop(
                    module = module,
                    exception =
                        result.exceptionOrNull()?.let {
                            it as? ChartboostCoreException
                                ?: ChartboostCoreException(ChartboostCoreError.InitializationError.Exception)
                        },
                )

            // If the adapter is a CMP, set it as the underlying CMP.
            if (module is ConsentAdapter) {
                if (consent.isAdapterAttached()) {
                    Utils.safeExecute {
                        observer?.onModuleInitializationCompleted(
                            ModuleInitializationResult(
                                moduleResult.start,
                                moduleResult.end,
                                moduleResult.duration,
                                ChartboostCoreException(ChartboostCoreError.InitializationError.ConsentAdapterPreviouslyInitialized),
                                module.moduleId,
                                module.moduleVersion,
                            ),
                        )
                    }
                } else {
                    consent.attachAdapter(context.applicationContext, module)
                }
            }

            // If the module is also a consent observer, automatically add it as an observer for
            // consent changes.
            if (module is ConsentObserver) {
                consent.addObserver(module)
            }

            // If a module is a PublisherMetadataObserver, automatically add it as an observer for
            // publisher metadata changes.
            if (module is EnvironmentObserver) {
                ChartboostCore.analyticsEnvironment.addObserver(module)
            }

            Utils.safeExecute {
                observer?.onModuleInitializationCompleted(moduleResult as ModuleInitializationResult)
            }
        }

        return result
    }

    /**
     * Creates a [ModuleConfiguration] object from the [SdkConfiguration] object.
     *
     * @param sdkConfiguration The [SdkConfiguration] object to use for creating the [ModuleConfiguration] object.
     */
    private fun createModuleInitializationConfiguration(sdkConfiguration: SdkConfiguration) =
        ModuleConfiguration(
            chartboostApplicationIdentifier = sdkConfiguration.chartboostApplicationIdentifier,
        )
}

/**
 * @suppress
 */
internal class ChartboostCoreModule(
    override val moduleId: String = "ChartboostCoreModule",
    override val moduleVersion: String = Constants.SDK_VERSION,
) : Module {
    override fun updateCredentials(
        context: Context,
        credentials: JSONObject,
    ) {}

    override suspend fun initialize(
        context: Context,
        moduleConfiguration: ModuleConfiguration,
    ): Result<Unit> {
        return Result.success(Unit)
    }
}
