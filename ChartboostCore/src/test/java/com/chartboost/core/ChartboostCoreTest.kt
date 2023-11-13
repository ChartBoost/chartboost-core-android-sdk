/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.chartboost.core.ChartboostCoreInternal.moduleInitializationStatuses
import com.chartboost.core.initialization.InitializableModule
import com.chartboost.core.initialization.InitializableModuleObserver
import com.chartboost.core.initialization.ModuleInitializationConfiguration
import com.chartboost.core.initialization.ModuleInitializationResult
import com.chartboost.core.initialization.ModuleInitializationStatus
import com.chartboost.core.initialization.SdkConfiguration
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
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
        sharedPreferences = context.getSharedPreferences(
            "com.chartboost.core.canary", Context.MODE_PRIVATE
        )
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
            val modules = mutableListOf<InitializableModule>()
            val observer = mock<InitializableModuleObserver>()

            repeat(randomNum) { index ->
                modules.add(TestModule("module_$index"))
            }

            ChartboostCore.initializeSdk(
                context,
                SdkConfiguration(chartboostApplicationIdentifier = ""),
                modules,
                observer
            )

            verify(observer, times(modules.size)).onModuleInitializationCompleted(anyOrNull())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `initializeSdk calls onModuleInitializationCompleted with a ChartboostCoreModuleInitializationResult`() =
        runTest {
            val context = mockk<Context>(relaxed = true)
            val sdkConfiguration = SdkConfiguration(chartboostApplicationIdentifier = "")
            val module = TestModule("test_module")

            val observer = mockk<InitializableModuleObserver>()
            val slot = slot<ModuleInitializationResult>()

            every { observer.onModuleInitializationCompleted(capture(slot)) } just Runs

            ChartboostCore.initializeSdk(context, sdkConfiguration, listOf(module), observer)

            verify(exactly = 1) { observer.onModuleInitializationCompleted(any()) }

            val result = slot.captured

            assertNotNull(result)
            assertEquals(module, result.module)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `if all modules succeed, their statuses should be set to initialized`() = runTest {
        val randomNum = (1..10).random()
        val modules = mutableListOf<InitializableModule>()
        val observer = mock<InitializableModuleObserver>()

        repeat(randomNum) { index ->
            modules.add(TestModule("module_$index"))
        }

        ChartboostCore.initializeSdk(
            context,
            SdkConfiguration(chartboostApplicationIdentifier = ""),
            modules,
            observer
        )

        modules.forEach {
            assertEquals(
                ModuleInitializationStatus.INITIALIZED,
                moduleInitializationStatuses[it.moduleId]?.get()
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `if all modules fail, their statuses should be set to not initialized`() = runTest {
        val randomNum = (1..10).random()
        val modules = mutableListOf<InitializableModule>()
        val observer = mockk<InitializableModuleObserver>(relaxed = true)

        repeat(randomNum) {
            // Override the module's initialize() method to throw an exception
            val module = object : InitializableModule {
                override val moduleId: String get() = "TestModule"
                override val moduleVersion: String get() = "1.0"

                override fun updateProperties(configuration: JSONObject) {}

                override suspend fun initialize(
                    context: Context,
                    moduleInitializationConfiguration: ModuleInitializationConfiguration
                ): Result<Unit> {
                    throw RuntimeException("Initialization failed")
                }
            }
            modules.add(module)
        }

        ChartboostCore.initializeSdk(
            context,
            SdkConfiguration(chartboostApplicationIdentifier = ""),
            modules,
            observer
        )

        modules.forEach {
            assertEquals(
                ModuleInitializationStatus.NOT_INITIALIZED,
                moduleInitializationStatuses[it.moduleId]?.get()
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `initializing one module sequentially calls onModuleInitializationCompleted n times for n completions`() =
        runTest {
            val context = mockk<Context>(relaxed = true)
            val sdkConfiguration = SdkConfiguration(chartboostApplicationIdentifier = "")
            val module = TestModule("test_module")
            val observer = mockk<InitializableModuleObserver>(relaxed = true)

            coEvery { observer.onModuleInitializationCompleted(any()) } just Runs

            val randomNum = (1..10).random()
            repeat(randomNum) {
                ChartboostCore.initializeSdk(context, sdkConfiguration, listOf(module), observer)
            }

            coVerify(exactly = randomNum) { observer.onModuleInitializationCompleted(any()) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `initializing a module multiple times while in progress should call onModuleInitializationCompleted once`() =
        runTest {
            val observer = mock<InitializableModuleObserver>()
            val modules = listOf(TestModule("TestModule"))

            // Initialize the module three times concurrently
            val tasks = List(3) {
                launch {
                    ChartboostCore.initializeSdk(
                        context,
                        SdkConfiguration(chartboostApplicationIdentifier = ""),
                        modules,
                        observer
                    )
                }
            }

            tasks.joinAll()
            verify(observer, times(1)).onModuleInitializationCompleted(anyOrNull())
        }
}

class TestModule(override val moduleId: String) : InitializableModule {
    override val moduleVersion: String = "1.0.0"

    override fun updateProperties(configuration: JSONObject) {}

    override suspend fun initialize(
        context: Context,
        moduleInitializationConfiguration: ModuleInitializationConfiguration
    ): Result<Unit> {
        delay(1000)
        return Result.success(Unit)
    }
}
