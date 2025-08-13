package com.hater.githubsearch.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hater.githubsearch.api.GithubSearchApi
import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.util.Constants.PAGING_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val api: GithubSearchApi
): SearchRepository {
    override fun searchBookPaging(query: String): Flow<PagingData<GithubUser>> {
        val pagingSourceFactory = { SearchPagingSource(api, query) }
        return Pager(
            config = PagingConfig(
                pageSize = PAGING_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

}