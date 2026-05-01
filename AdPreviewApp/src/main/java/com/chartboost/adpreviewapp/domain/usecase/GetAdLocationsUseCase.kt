/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.domain.usecase

import com.chartboost.adpreviewapp.data.model.AdLocation
import com.chartboost.adpreviewapp.domain.repository.AppsRepository
import javax.inject.Inject

class GetAdLocationsUseCase
    @Inject
    constructor(
        private val repository: AppsRepository,
    ) {
        suspend operator fun invoke(appId: String): List<AdLocation> {
            return repository.getAdLocations(appId)
        }
    }
