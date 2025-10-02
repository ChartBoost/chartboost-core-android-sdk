/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.testscreen

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AdEventsLogger {
    private val _eventLog = MutableStateFlow<List<String>>(emptyList())
    val eventLog = _eventLog.asStateFlow()

    fun logEvent(
        eventName: String,
        location: String? = null,
    ) {
        _eventLog.update {
            val message = formatEvent(location, eventName)
            it + message
        }
    }

    private fun formatEvent(
        location: String?,
        eventName: String,
    ): String {
        return if (location.isNullOrBlank()) eventName else "$location $eventName"
    }

    fun clear() {
        _eventLog.value = emptyList()
    }
}
