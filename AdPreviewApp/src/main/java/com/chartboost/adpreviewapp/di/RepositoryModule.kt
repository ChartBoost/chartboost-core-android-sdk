/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.di

import com.chartboost.adpreviewapp.data.repository.AppsRepositoryImpl
import com.chartboost.adpreviewapp.data.repository.KeywordsRepositoryImpl
import com.chartboost.adpreviewapp.domain.repository.AppsRepository
import com.chartboost.adpreviewapp.domain.repository.KeywordsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAppsRepository(impl: AppsRepositoryImpl): AppsRepository

    @Binds
    @Singleton
    abstract fun bindKeywordsRepository(impl: KeywordsRepositoryImpl): KeywordsRepository
}
