package com.hater.githubsearch.di

import com.hater.githubsearch.repository.FakeSearchRepository
import com.hater.githubsearch.repository.SearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {
    @Provides
    @Singleton
    fun provideSearchRepository(): SearchRepository {
        return FakeSearchRepository()
    }
}
