package com.hater.githubsearch.repository

import androidx.paging.PagingData
import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.UserInfo
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface SearchRepository {
    fun searchBookPaging(query: String): Flow<PagingData<UserInfo>>

    suspend fun searchRepo(query: String): Response<GithubUserRepo>

}