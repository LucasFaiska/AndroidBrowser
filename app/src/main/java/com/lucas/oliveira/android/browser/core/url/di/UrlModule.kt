package com.lucas.oliveira.android.browser.core.url.di

import com.lucas.oliveira.android.browser.core.url.DeterministicUrlResolver
import com.lucas.oliveira.android.browser.core.url.GoogleSearchEngineProvider
import com.lucas.oliveira.android.browser.core.url.SearchEngineProvider
import com.lucas.oliveira.android.browser.core.url.UrlResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UrlModule {

    @Binds
    @Singleton
    abstract fun bindUrlResolver(
        deterministicUrlResolver: DeterministicUrlResolver
    ): UrlResolver

    @Binds
    @Singleton
    abstract fun bindSearchEngineProvider(
        googleSearchEngineProvider: GoogleSearchEngineProvider
    ): SearchEngineProvider
}
