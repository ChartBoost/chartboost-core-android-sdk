/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.service

import android.app.Application
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import javax.inject.Inject

class SystemCredentialsServiceImpl
    @Inject
    constructor(
        private val application: Application,
    ) : SystemCredentialsService {
        override suspend fun fetchSystemSavedPassword(preferImmediatelyAvailable: Boolean): SystemCredentialFetchResult {
            return try {
                val credentialManager = CredentialManager.Companion.create(application)
                val credentialRequest =
                    GetCredentialRequest(
                        credentialOptions = listOf(GetPasswordOption()),
                        preferImmediatelyAvailableCredentials = preferImmediatelyAvailable,
                    )
                val credential =
                    credentialManager.getCredential(
                        application,
                        credentialRequest,
                    ).credential as? PasswordCredential
                        ?: return SystemCredentialFetchResult.None
                SystemCredentialFetchResult.Success(credential.id, credential.password)
            } catch (_: NoCredentialException) {
                Log.i(TAG, "No credentials found")
                SystemCredentialFetchResult.None
            } catch (_: GetCredentialCancellationException) {
                Log.i(TAG, "User canceled credential selection")
                SystemCredentialFetchResult.Canceled
            } catch (e: GetCredentialException) {
                Log.w(TAG, "CredentialManager error: ${e.type}", e)
                SystemCredentialFetchResult.Error(e)
            } catch (t: Throwable) {
                Log.e(TAG, "CredentialManager failure", t)
                SystemCredentialFetchResult.Error(t)
            }
        }

        private companion object {
            private const val TAG = "SystemCredentials"
        }
    }
