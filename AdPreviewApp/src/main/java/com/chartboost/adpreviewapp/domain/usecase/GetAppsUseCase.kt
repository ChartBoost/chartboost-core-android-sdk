/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.domain.usecase

import com.chartboost.adpreviewapp.data.model.App
import com.chartboost.adpreviewapp.domain.repository.AppsRepository
import javax.inject.Inject

class GetAppsUseCase
    @Inject
    constructor(
        private val repository: AppsRepository,
    ) {
        suspend operator fun invoke(): List<App> = repository.getApps()
    }
