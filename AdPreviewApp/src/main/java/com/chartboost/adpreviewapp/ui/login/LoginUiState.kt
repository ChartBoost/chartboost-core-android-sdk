/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = false,
    val isLoginEnabled: Boolean = false,
    val loginState: LoginState = LoginState.LoggedOut,
    val errorType: LoginErrorType? = null,
)

sealed class LoginErrorType {
    data object Network : LoginErrorType()

    data object InvalidCredentials : LoginErrorType()

    data object Unknown : LoginErrorType()
}
