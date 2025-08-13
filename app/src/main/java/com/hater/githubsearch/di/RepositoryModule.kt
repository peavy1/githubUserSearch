package com.hater.githubsearch.di

import com.hater.githubsearch.repository.SearchRepository
import com.hater.githubsearch.repository.SearchRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindGithubSearchRepository(
        bookSearchRepositoryImpl: SearchRepositoryImpl,
    ): SearchRepository
}