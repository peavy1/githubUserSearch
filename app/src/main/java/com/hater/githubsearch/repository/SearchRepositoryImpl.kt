package com.hater.githubsearch.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hater.githubsearch.api.GithubSearchApi
import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.UserInfo
import com.hater.githubsearch.util.Constants.PAGING_SIZE
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val api: GithubSearchApi
): SearchRepository {
    override fun searchUserPaging(query: String): Flow<PagingData<UserInfo>> {
        val pagingSourceFactory = { SearchPagingSource(api, query) }
        return Pager(
            config = PagingConfig(
                pageSize = PAGING_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    override suspend fun searchRepo(query: String): Response<GithubUserRepo> {
        return api.getUserRepoCount(query)
    }


}