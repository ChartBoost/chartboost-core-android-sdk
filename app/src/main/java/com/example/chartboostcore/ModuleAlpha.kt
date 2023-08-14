package com.example.chartboostcore

import android.content.Context
import com.chartboost.chartboostcore.initialization.ChartboostCoreInitializableModule
import com.chartboost.chartboostcore.initialization.ChartboostCoreModuleConfiguration
import kotlinx.coroutines.delay

/**
 * This is a sample implementation of a module to demonstrate how a module can be designed.
 * It is not an actual module and should not be used as such. It also does not do anything useful.
 *
 * @property appId The app ID.
 * @property someOtherId Some other ID.
 */
class ModuleAlpha(private val appId: String, private val someOtherId: String) :
    ChartboostCoreInitializableModule {
    /**
     * The ID of the module. This is recommended to be unique.
     */
    override val moduleId: String = "Alpha"

    /**
     * The version of the module. This is recommended to be a semantic version e.g. 1.0.0.
     */
    override val moduleVersion: String = "1.0.0"

    /**
     * Override this function to initialize the module.
     *
     * @param context The [Context] to use for initialization.
     * @param config The [ChartboostCoreModuleConfiguration] to use for initialization.
     */
    override suspend fun initialize(
        context: Context,
        config: ChartboostCoreModuleConfiguration,
    ): Result<Unit> {
        // Initialize your module here.
        // This example simulates a module initialization by adding a small delay.
        delay(500)

        // Depending on the result of the initialization, return either Result.success or Result.failure(Throwable).
        // For simplicity, this example always returns Result.success to indicate a successful initialization.
        return Result.success(Unit)
    }
}
