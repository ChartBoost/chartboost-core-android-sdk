/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.repository

import com.chartboost.adpreviewapp.domain.repository.KeywordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeywordsRepositoryImpl
    @Inject
    constructor() : KeywordsRepository {
        private val _keywords = MutableStateFlow<Map<String, String>>(emptyMap())

        override fun getKeywords(): Flow<Map<String, String>> = _keywords.asStateFlow()

        override fun addKeyword(
            key: String,
            value: String,
        ) {
            _keywords.value =
                _keywords.value.toMutableMap().apply {
                    put(key, value)
                }
        }

        override fun removeKeyword(key: String) {
            _keywords.value =
                _keywords.value.toMutableMap().apply {
                    remove(key)
                }
        }

        override fun clearKeywords() {
            _keywords.value = emptyMap()
        }
    }
