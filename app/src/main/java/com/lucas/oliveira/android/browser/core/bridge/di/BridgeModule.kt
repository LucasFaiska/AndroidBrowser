package com.lucas.oliveira.android.browser.core.bridge.di

import com.lucas.oliveira.android.browser.core.bridge.BridgePermissionStore
import com.lucas.oliveira.android.browser.core.bridge.SharedPreferencesBridgePermissionStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BridgeModule {

    @Binds
    @Singleton
    abstract fun bindBridgePermissionStore(
        sharedPreferencesBridgePermissionStore: SharedPreferencesBridgePermissionStore
    ): BridgePermissionStore
}
