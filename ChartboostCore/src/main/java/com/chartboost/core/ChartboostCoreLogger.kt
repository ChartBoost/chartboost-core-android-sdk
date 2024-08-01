/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core

import android.util.Log

/**
 * A logger for Chartboost Core SDK and its modules.
 */
object ChartboostCoreLogger {
    /**
     * The log level for Chartboost Core logging purposes.
     */
    var logLevel: ChartboostCoreLogLevel = ChartboostCoreLogLevel.INFO
        get() = serverLogLevelOverride ?: field

    /**
     * The log level override set by the server or a Charles rewrite rule.
     */
    internal var serverLogLevelOverride: ChartboostCoreLogLevel? = null

    /**
     * The tag used for Chartboost Core and module logging purposes.
     */
    private const val TAG = "[ChartboostCore]"

    /**
     * Log a debug message.
     */
    fun d(msg: String) {
        if (shouldLog(ChartboostCoreLogLevel.DEBUG)) {
            log(Log.DEBUG, msg)
        }
    }

    /**
     * Log an info message.
     */
    fun i(msg: String) {
        if (shouldLog(ChartboostCoreLogLevel.INFO)) {
            log(Log.INFO, msg)
        }
    }

    /**
     * Log a warning message.
     */
    fun w(msg: String) {
        if (shouldLog(ChartboostCoreLogLevel.WARNING)) {
            log(Log.WARN, msg)
        }
    }

    /**
     * Log an error message.
     */
    fun e(msg: String) {
        if (shouldLog(ChartboostCoreLogLevel.ERROR)) {
            log(Log.ERROR, msg)
        }
    }

    /**
     * Log a message to the console and to a server endpoint.
     *
     * @param level The log level.
     * @param msg The message to log.
     */
    private fun log(
        level: Int,
        msg: String,
    ) {
        val callerClassName = Throwable().stackTrace[2].className
        val callerMethodName = Throwable().stackTrace[2].methodName
        val formattedMsg = "[$callerClassName.$callerMethodName] $msg"

        Log.println(level, TAG, formattedMsg)
        logToServer(level, formattedMsg)
    }

    /**
     * Log to a server endpoint. This method is a stub and will be implemented later.
     */
    private fun logToServer(
        level: Int,
        msg: String,
    ) {
        // Implement this method to log to a server endpoint.
    }

    /**
     * Check if a log level should be logged.
     */
    private fun shouldLog(level: ChartboostCoreLogLevel): Boolean {
        return level.value <= logLevel.value
    }
}
