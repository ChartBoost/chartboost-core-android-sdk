/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import com.chartboost.core.error.ChartboostCoreError
import com.chartboost.core.error.ChartboostCoreException
import com.chartboost.core.initialization.Module
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ResultManagerTest {
    @Test
    fun `start stores the moduleId in resultDataMap`() {
        val module = mockk<Module>()
        ResultManager.start(module)

        assertTrue(ResultManager.resultDataMap.containsKey(module))
    }

    @Test
    fun `result data has all the expected fields`() {
        val module = mockk<Module>()
        every { module.moduleId } returns "module"
        every { module.moduleVersion } returns "1.0.0"

        val exception = ChartboostCoreException(ChartboostCoreError.InitializationError.Unknown)
        val start = System.currentTimeMillis()

        ResultManager.start(module)
        val end = System.currentTimeMillis()
        val result = ResultManager.stop(module, exception)

        checkNotNull(result.start)
        assertTrue(result.start >= start)

        checkNotNull(result.end)
        assertTrue(result.end >= end)

        checkNotNull(result.exception)
        assertEquals(exception, result.exception)

        checkNotNull(result.duration)
        assertTrue(result.duration >= 0)
    }

    @Test
    fun `stop returns distinct result data for each module`() {
        val module1 = mockk<Module>()
        every { module1.moduleId } returns "module1"
        every { module1.moduleVersion } returns "1.0.0"
        val module2 = mockk<Module>()
        every { module2.moduleId } returns "module2"
        every { module2.moduleVersion } returns "2.0.0"

        val exception1 = ChartboostCoreException(ChartboostCoreError.InitializationError.Unknown)
        val exception2 = ChartboostCoreException(ChartboostCoreError.InitializationError.Exception)

        ResultManager.start(module1)
        val result1 = ResultManager.stop(module1, exception1)

        ResultManager.start(module2)
        val result2 = ResultManager.stop(module2, exception2)

        assertNotSame(result1.exception, result2.exception)
    }

    @Test
    fun `stop returns the same result data for the same module`() {
        val module = mockk<Module>()
        every { module.moduleId } returns "module"
        every { module.moduleVersion } returns "1.0.0"
        val exception = ChartboostCoreException(ChartboostCoreError.InitializationError.Unknown)

        ResultManager.start(module)
        val result1 = ResultManager.stop(module, exception)

        ResultManager.start(module)
        val result2 = ResultManager.stop(module, exception)

        assertEquals(result1.exception, result2.exception)
    }
}
