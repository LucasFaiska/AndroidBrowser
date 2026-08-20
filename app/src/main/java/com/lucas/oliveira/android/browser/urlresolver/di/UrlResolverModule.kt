package com.lucas.oliveira.android.browser.urlresolver.di

import com.lucas.oliveira.android.browser.urlresolver.DeterministicUrlResolver
import com.lucas.oliveira.android.browser.urlresolver.UrlResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UrlResolverModule {

    @Binds
    @Singleton
    abstract fun bindUrlResolver(
        deterministicUrlResolver: DeterministicUrlResolver
    ): UrlResolver
}
