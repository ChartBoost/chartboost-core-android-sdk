/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.result.Credentials
import com.chartboost.adpreviewapp.data.local.AuthCredentialsStore
import com.chartboost.adpreviewapp.data.model.AuthCredentials
import com.chartboost.adpreviewapp.service.SystemCredentialFetchResult
import com.chartboost.adpreviewapp.service.SystemCredentialsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val auth0Config: Auth0Config,
        private val authCredentialsStore: AuthCredentialsStore,
        private val authApiClient: AuthenticationAPIClient,
        private val systemCredentialsService: SystemCredentialsService,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                LoginUiState(
                    email = "",
                    password = "",
                    isEmailValid = false,
                    isLoginEnabled = false,
                    loginState = LoginState.LoggedOut,
                ),
            )
        val uiState: StateFlow<LoginUiState> = _uiState

        fun onEmailUpdated(email: String) {
            _uiState.update {
                val trimmedEmail = email.trim()
                val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
                it.copy(
                    email = trimmedEmail,
                    isEmailValid = isEmailValid,
                    isLoginEnabled = isEmailValid && it.password.isNotBlank(),
                    errorType = null,
                )
            }
        }

        fun onPasswordUpdated(password: String) {
            _uiState.update {
                it.copy(
                    password = password,
                    isLoginEnabled = it.email.isNotBlank() && it.isEmailValid && password.isNotBlank(),
                    errorType = null,
                )
            }
        }

        fun loginWithCredentials(
            email: String,
            password: String,
        ) {
            _uiState.update {
                it.copy(
                    loginState = LoginState.InProgress,
                    errorType = null,
                )
            }

            val connection = "Username-Password-Authentication"

            authApiClient
                .login(email, password, connection)
                .setScope(auth0Config.scope)
                .setAudience(auth0Config.audience)
                .validateClaims()
                .start(
                    object : Callback<Credentials, AuthenticationException> {
                        override fun onFailure(error: AuthenticationException) {
                            val rawDescription = error.getDescription().lowercase()

                            val errorType =
                                when {
                                    error.isNetworkError -> {
                                        Log.e(TAG, "Login failed due to network error", error)
                                        LoginErrorType.Network
                                    }
                                    "wrong email or password" in rawDescription -> {
                                        Log.e(TAG, "Login failed: wrong credentials", error)
                                        LoginErrorType.InvalidCredentials
                                    }
                                    else -> {
                                        Log.e(TAG, "Login failed: $rawDescription", error)
                                        LoginErrorType.Unknown
                                    }
                                }

                            _uiState.update {
                                it.copy(
                                    loginState = LoginState.LoggedOut,
                                    errorType = errorType,
                                )
                            }
                        }

                        override fun onSuccess(result: Credentials) {
                            Log.d(TAG, "Native login success for user: $email")
                            if (validateCredentials(result)) {
                                saveCredentials(result)
                                _uiState.update {
                                    it.copy(loginState = LoginState.LoggedIn)
                                }
                            } else {
                                Log.w(TAG, "Invalid credentials received — accessToken is blank")
                                _uiState.update {
                                    it.copy(
                                        loginState = LoginState.LoggedOut,
                                        errorType = LoginErrorType.Unknown,
                                    )
                                }
                            }
                        }
                    },
                )
        }

        private fun validateCredentials(credentials: Credentials): Boolean = credentials.accessToken.isNotBlank()

        private fun saveCredentials(credentials: Credentials) {
            viewModelScope.launch {
                with(credentials) {
                    expiresAt.time.let { time ->
                        authCredentialsStore.save(
                            AuthCredentials(
                                accessToken = accessToken,
                                idToken = idToken,
                                refreshToken = refreshToken,
                                expiresAt = time,
                            ),
                        )
                    }
                }
            }
        }

        fun requestSystemSavedCredentials() {
            val currentUiState = uiState.value
            if (currentUiState.loginState != LoginState.LoggedOut) return
            if (currentUiState.email.isNotBlank() || currentUiState.password.isNotBlank()) return

            viewModelScope.launch {
                when (val fetchResult = systemCredentialsService.fetchSystemSavedPassword()) {
                    is SystemCredentialFetchResult.Success -> {
                        onEmailUpdated(fetchResult.email)
                        onPasswordUpdated(fetchResult.password)
                    }
                    // None / Canceled / Error -> ignore for v1 (or emit a message later)
                    else -> Unit
                }
            }
        }

        private companion object {
            private const val TAG = "LoginViewModel"
        }
    }
