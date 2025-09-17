package com.hater.githubsearch.repository


import android.util.Log
import com.hater.githubsearch.model.GithubUser

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hater.githubsearch.api.GithubApi
import com.hater.githubsearch.api.GithubSearchApi
import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.UserInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull


class SearchPagingSource(
    private val api: GithubSearchApi,
    private val query: String
) : PagingSource<Int, UserInfo>() {


    override fun getRefreshKey(state: PagingState<Int, UserInfo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserInfo> {
        return try {
            val pageNumber = params.key ?: STARTING_PAGE_INDEX
            val searchUserList = GithubApi.searchUser(query, pageNumber)?.items ?: emptyList()

            val repoCountsDeferred = coroutineScope {
                searchUserList.map { user ->
                    async {
                        GithubApi.getUserRepoCount(user.login)
                    }
                }
            }

            val repoCountList = repoCountsDeferred.awaitAll().filterNotNull()

            val userinfo = mutableListOf<UserInfo>()
            searchUserList.map { user ->
                repoCountList.firstOrNull { user.login == it.login }?.let { findUserRepo ->
                    userinfo.add(
                        UserInfo(
                            login = user.login,
                            id = user.id,
                            avatarUrl = user.avatarUrl,
                            htmlUrl = user.htmlUrl,
                            publicRepoCount = findUserRepo.publicRepoCount
                        )
                    )
                }
            }

            val prevKey = if (pageNumber == STARTING_PAGE_INDEX) { null } else { pageNumber - 1 }
            val nextKey = if (userinfo.isEmpty()) { null } else { pageNumber + 1 }
            LoadResult.Page(
                data = userinfo,
                prevKey = prevKey,
                nextKey = nextKey,
            )
        }
        catch (exception: Exception) { LoadResult.Error(exception) }
    }


    companion object {
        const val STARTING_PAGE_INDEX = 1
    }
}
