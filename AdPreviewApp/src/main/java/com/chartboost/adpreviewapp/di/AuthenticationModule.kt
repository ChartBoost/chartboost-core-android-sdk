/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.di

import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.chartboost.adpreviewapp.BuildConfig
import com.chartboost.adpreviewapp.ui.login.Auth0Config
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {
    private const val AUTH0_SCOPE = "openid profile email"
    private const val AUTH0_AUDIENCE = "https://api.gateway.chartboost.com"

    @Provides
    @Singleton
    fun provideAuth0(): Auth0 =
        Auth0(
            BuildConfig.AUTH0_CLIENT_ID,
            BuildConfig.AUTH0_DOMAIN,
        )

    @Provides
    fun provideAuth0Config(
        @Auth0Scheme auth0Scheme: String,
    ): Auth0Config =
        Auth0Config(
            scheme = auth0Scheme,
            scope = AUTH0_SCOPE,
            audience = AUTH0_AUDIENCE,
        )

    @Provides
    @Auth0Scheme
    fun provideAuth0Scheme(): String = BuildConfig.AUTH0_SCHEME

    @Provides
    @Singleton
    fun provideAuthenticationApiClient(auth0: Auth0): AuthenticationAPIClient = AuthenticationAPIClient(auth0)
}
