package com.hater.githubsearch.repository


import androidx.paging.PagingData
import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.UserInfo
import com.hater.githubsearch.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import retrofit2.Response
import javax.inject.Inject

class FakeSearchRepository @Inject constructor() : SearchRepository {

    private val pagingDataFlow = MutableStateFlow<PagingData<UserInfo>?>(null)

    override fun searchUserPaging(query: String): Flow<PagingData<UserInfo>> {
        return pagingDataFlow.filterNotNull()
    }

    override suspend fun searchRepo(query: String): Response<GithubUserRepo> {
        return Response.success(GithubUserRepo(login = query, publicRepoCount = 0))
    }

    suspend fun setNextPagingData(pagingData: PagingData<UserInfo>) {
        pagingDataFlow.emit(pagingData)
    }
}

