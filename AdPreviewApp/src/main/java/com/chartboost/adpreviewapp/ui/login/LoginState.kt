/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.login

sealed class LoginState {
    data object LoggedOut : LoginState()

    data object InProgress : LoginState()

    data object LoggedIn : LoginState()
}
