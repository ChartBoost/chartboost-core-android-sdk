/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.chartboost.adpreviewapp.data.local.AuthCredentialsStore
import com.chartboost.adpreviewapp.data.local.AuthCredentialsStoreImpl
import com.chartboost.adpreviewapp.data.local.SettingsPreferencesImpl
import com.chartboost.adpreviewapp.domain.SettingsPreferences
import com.chartboost.adpreviewapp.util.authDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {
    companion object {
        @Provides
        @Singleton
        fun providePreferencesDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> = context.authDataStore
    }

    @Binds
    @Singleton
    abstract fun provideAuthCredentialsStore(authCredentialsStoreImpl: AuthCredentialsStoreImpl): AuthCredentialsStore

    @Binds
    @Singleton
    abstract fun bindSettingsPreferences(impl: SettingsPreferencesImpl): SettingsPreferences
}
