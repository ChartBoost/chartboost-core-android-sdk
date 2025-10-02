/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsDebugFlag

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Auth0Scheme

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MonetizationSdkVersion
