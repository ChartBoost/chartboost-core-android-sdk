/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.launcher

sealed class LauncherState {
    data object Checking : LauncherState()

    data object LoggedIn : LauncherState()

    data object LoggedOut : LauncherState()
}
