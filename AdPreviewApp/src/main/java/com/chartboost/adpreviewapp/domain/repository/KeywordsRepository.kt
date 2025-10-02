/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface KeywordsRepository {
    fun getKeywords(): Flow<Map<String, String>>

    fun addKeyword(
        key: String,
        value: String,
    )

    fun removeKeyword(key: String)

    fun clearKeywords()
}
