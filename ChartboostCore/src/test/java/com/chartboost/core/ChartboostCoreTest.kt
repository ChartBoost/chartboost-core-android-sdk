/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.chartboost.core.ChartboostCoreInternal.moduleInitializationStatuses
import com.chartboost.core.initialization.*
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdClient
import com.google.android.gms.appset.AppSetIdInfo
import com.google.android.gms.tasks.Task
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper
import kotlin.time.Duration.Companion.milliseconds

@RunWith(RobolectricTestRunner::class)
class ChartboostCoreTest {
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        sharedPreferences =
            context.getSharedPreferences(
                "com.chartboost.core.canary",
                Context.MODE_PRIVATE,
            )

        mockkStatic(AppSet::class)
        val mockAppSetIdInfo: AppSetIdInfo = mockk()
        val mockAppSetIdInfoTask: Task<AppSetIdInfo> = mockk()
        val mockAppSetIdClient: AppSetIdClient = mockk()
        every { mockAppSetIdInfo.scope } returns AppSetIdInfo.SCOPE_DEVELOPER
        every { mockAppSetIdInfo.id } returns "app_set_id"
        every { mockAppSetIdInfoTask.isComplete } returns true
        every { mockAppSetIdInfoTask.isSuccessful } returns true
        every { mockAppSetIdInfoTask.result } returns mockAppSetIdInfo
        every { mockAppSetIdClient.appSetIdInfo } returns mockAppSetIdInfoTask
        every { AppSet.getClient(any()) } returns mockAppSetIdClient
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `initializing n modules once should call onModuleInitializationCompleted n times`() =
        runTest {
            val randomNum = (1..10).random()
            val modules = mutableListOf<Module>()
            val observer = mock<ModuleObserver>()

            repeat(randomNum) { index ->
                modules.add(TestModule("module_$index"))
            }

            ChartboostCore.initializeSdk(
                context,
                SdkConfiguration(
                    chartboostApplicationIdentifier = "",
                    modules = modules,
                ),
                observer,
            )

            verify(observer, times(modules.size)).onModuleInitializationCompleted(anyOrNull())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `initializeSdk calls onModuleInitializationCompleted with a ChartboostCoreModuleInitializationResult`() =
        runTest {
            val context = mockk<Context>(relaxed = true)
            val module = TestModule("test_module")
            val sdkConfiguration =
                SdkConfiguration(chartboostApplicationIdentifier = "", modules = listOf(module))

            val observer = mockk<ModuleObserver>()
            val slot = slot<ModuleInitializationResult>()

            every { observer.onModuleInitializationCompleted(capture(slot)) } just Runs

            ChartboostCore.initializeSdk(context, sdkConfiguration, observer)

            verify(exactly = 1) { observer.onModuleInitializationCompleted(any()) }

            val result = slot.captured

            assertNotNull(result)
            assertEquals(module.moduleId, result.moduleId)
            assertEquals(module.moduleVersion, result.moduleVersion)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `if all modules succeed, their statuses should be set to initialized`() =
        runTest {
            val randomNum = (1..10).random()
            val modules = mutableListOf<Module>()
            val observer = mock<ModuleObserver>()

            repeat(randomNum) { index ->
                modules.add(TestModule("module_$index"))
            }

            ChartboostCore.initializeSdk(
                context,
                SdkConfiguration(chartboostApplicationIdentifier = "", modules = modules),
                observer,
            )

            modules.forEach {
                assertEquals(
                    ModuleInitializationStatus.INITIALIZED,
                    moduleInitializationStatuses[it.moduleId]?.get(),
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `if all modules fail, their statuses should be set to not initialized`() =
        runTest {
            val randomNum = (1..10).random()
            val modules = mutableListOf<Module>()
            val observer = mockk<ModuleObserver>(relaxed = true)

            repeat(randomNum) {
                // Override the module's initialize() method to throw an exception
                val module =
                    object : Module {
                        override val moduleId: String get() = "TestModule"
                        override val moduleVersion: String get() = "1.0"

                        override fun updateCredentials(
                            context: Context,
                            credentials: JSONObject,
                        ) {
                        }

                        override suspend fun initialize(
                            context: Context,
                            moduleConfiguration: ModuleConfiguration,
                        ): Result<Unit> {
                            throw RuntimeException("Initialization failed")
                        }
                    }
                modules.add(module)
            }

            ChartboostCore.initializeSdk(
                context,
                SdkConfiguration(chartboostApplicationIdentifier = "", modules = modules),
                observer,
            )

            modules.forEach {
                assertEquals(
                    ModuleInitializationStatus.NOT_INITIALIZED,
                    moduleInitializationStatuses[it.moduleId]?.get(),
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `initializing one module sequentially calls onModuleInitializationCompleted n times for n completions`() =
        runTest(timeout = 60000.milliseconds) {
            val context = mockk<Context>(relaxed = true)
            val module = TestModule("test_module")
            val sdkConfiguration =
                SdkConfiguration(chartboostApplicationIdentifier = "", modules = listOf(module))
            val observer = mockk<ModuleObserver>(relaxed = true)

            coEvery { observer.onModuleInitializationCompleted(any()) } just Runs

            val randomNum = (2..8).random()
            repeat(randomNum) {
                ChartboostCore.initializeSdk(context, sdkConfiguration, observer)
            }
            ShadowLooper.idleMainLooper()

            coVerify(exactly = randomNum) { observer.onModuleInitializationCompleted(any()) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `initializing a module multiple times while in progress should call onModuleInitializationCompleted once`() =
        runTest {
            val observer = mock<ModuleObserver>()
            val modules = listOf(TestModule("TestModule"))

            // Initialize the module three times concurrently
            val tasks =
                List(3) {
                    launch {
                        ChartboostCore.initializeSdk(
                            context,
                            SdkConfiguration(chartboostApplicationIdentifier = "", modules = modules),
                            observer,
                        )
                    }
                }

            tasks.joinAll()
            verify(observer, times(1)).onModuleInitializationCompleted(anyOrNull())
        }
}

class TestModule(override val moduleId: String) : Module {
    override val moduleVersion: String = "1.0.0"

    override fun updateCredentials(
        context: Context,
        credentials: JSONObject,
    ) {}

    override suspend fun initialize(
        context: Context,
        moduleConfiguration: ModuleConfiguration,
    ): Result<Unit> {
        delay(200)
        return Result.success(Unit)
    }
}
