/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.keywords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chartboost.adpreviewapp.domain.repository.KeywordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeywordsViewModel
    @Inject
    constructor(
        private val keywordsRepository: KeywordsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(KeywordsUiState())
        val uiState = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                keywordsRepository.getKeywords().collectLatest { keywords ->
                    _uiState.update { it.copy(keywords = keywords) }
                }
            }
        }

        fun buttonClicked() {
            _uiState.update { it.copy(isAddKeywordDialogVisible = true) }
        }

        fun onKeyChanged(key: String) {
            _uiState.update { it.copy(keyInput = key) }
        }

        fun onValueChanged(value: String) {
            _uiState.update { it.copy(valueInput = value) }
        }

        fun onDialogDismiss() {
            _uiState.update {
                it.copy(
                    isAddKeywordDialogVisible = false,
                    keyInput = "",
                    valueInput = "",
                )
            }
        }

        fun onSaveKeyword() {
            val currentState = _uiState.value
            if (currentState.keyInput.isNotBlank() && currentState.valueInput.isNotBlank()) {
                viewModelScope.launch {
                    keywordsRepository.addKeyword(currentState.keyInput, currentState.valueInput)
                    onDialogDismiss()
                }
            }
        }

        fun onDeleteAllClicked() {
            _uiState.update { it.copy(isDeleteAllDialogVisible = true) }
        }

        fun onDeleteAllConfirmed() {
            viewModelScope.launch {
                keywordsRepository.clearKeywords()
                _uiState.update { it.copy(isDeleteAllDialogVisible = false) }
            }
        }

        fun onDeleteAllDialogDismiss() {
            _uiState.update { it.copy(isDeleteAllDialogVisible = false) }
        }
    }
