/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.domain.usecase

import com.chartboost.adpreviewapp.domain.repository.AppsRepository
import javax.inject.Inject

class ClearAppsRepositoryUseCase
    @Inject
    constructor(private val appsRepository: AppsRepository) {
        operator fun invoke() {
            appsRepository.clearCache()
        }
    }
