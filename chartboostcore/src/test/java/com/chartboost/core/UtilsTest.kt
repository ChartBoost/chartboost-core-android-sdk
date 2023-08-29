/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import android.content.Context
import com.chartboost.core.Constants.MAX_RETRY_ATTEMPTS
import com.chartboost.core.Constants.SEED_RETRY_DELAY_MS
import com.chartboost.core.Utils.executeWithExponentialBackoff
import com.chartboost.core.error.ChartboostCoreError
import com.chartboost.core.error.ChartboostCoreException
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UtilsTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exponential backoff succeeds after one attempt if action succeeds`() = runTest {
        val action: suspend (Int) -> Result<Int> = { Result.success(it) }
        val result = executeWithExponentialBackoff(action)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exponential backoff fails after max attempts`() = runTest {
        val action: suspend (Int) -> Result<Int> = { Result.failure(Exception("Test exception")) }
        val result = executeWithExponentialBackoff(action)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ChartboostCoreException)

        val exception = result.exceptionOrNull() as ChartboostCoreException
        assertTrue(exception.error is ChartboostCoreError.CoreError.ExponentialBackoffRetriesExhausted)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exponential backoff takes at least the minimum expected duration`() = runTest {
        var attempts = 0

        // Fail for MAX_RETRY_ATTEMPTS times, then succeed.
        val action: suspend (Int) -> Result<Int> = {
            attempts++
            if (attempts <= MAX_RETRY_ATTEMPTS) Result.failure(Exception("Test exception")) else Result.success(
                it
            )
        }

        val expectedMinDuration =
            (0 until MAX_RETRY_ATTEMPTS - 1).sumOf { SEED_RETRY_DELAY_MS shl it }

        advanceTimeBy(expectedMinDuration)

        val result = executeWithExponentialBackoff(action)
        assertTrue(result.isSuccess)

        val executedDuration = currentTime

        println("executedDuration: $executedDuration, expectedMinDuration: $expectedMinDuration")
        assertTrue(executedDuration >= expectedMinDuration)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `exponential backoff can handle exceptions thrown by action`() = runTest {
        val context = mockk<Context>(relaxed = true)

        val exceptionMessage = "Test exception"
        val action: suspend (Int) -> Result<Int> = { throw Exception(exceptionMessage) }
        val result = executeWithExponentialBackoff(action)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ChartboostCoreException)
        assertTrue((result.exceptionOrNull() as ChartboostCoreException).error is ChartboostCoreError.CoreError.ExponentialBackoffRetriesExhausted)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `safeExecute runs given block`() = runTest {
        val testDispatcher = StandardTestDispatcher()
        val testJob = Job()

        var wasBlockExecuted = false
        val block: suspend () -> Unit = { wasBlockExecuted = true }

        Utils.safeExecute(
            dispatcher = testDispatcher,
            block = block
        )

        // Advances the StandardTestDispatcher to the point where there are no tasks left to execute
        testDispatcher.scheduler.advanceUntilIdle()

        println("wasBlockExecuted: $wasBlockExecuted")
        assertTrue(wasBlockExecuted)

        testJob.cancel()
    }
}
