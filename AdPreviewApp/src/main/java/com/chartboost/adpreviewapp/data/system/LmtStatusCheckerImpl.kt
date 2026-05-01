/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.system

import com.chartboost.adpreviewapp.domain.system.LmtStatusChecker
import com.chartboost.adpreviewapp.ui.settings.LmtStatus
import javax.inject.Inject

// TODO HB-10086
// will be resolved within the ticket for fix version 1.1.0 by integrating Google UMP
class LmtStatusCheckerImpl
    @Inject
    constructor() : LmtStatusChecker {
        override suspend fun getStatus(): LmtStatus {
            // real system call for now stub
            return LmtStatus.GRANTED
        }
    }
