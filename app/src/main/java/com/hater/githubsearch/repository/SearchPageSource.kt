package com.hater.githubsearch.repository


import android.util.Log
import com.hater.githubsearch.model.GithubUser

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hater.githubsearch.api.GithubSearchApi
import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.UserInfo
import com.hater.githubsearch.util.Constants.USER_NAME_QUALIFIER
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
            val result = api.searchUser(query, pageNumber)
            val searchUserList = result.body()?.items ?: emptyList()

            val repoCountsDeferred = coroutineScope {
                searchUserList.map { user ->
                    async {
                        api.getUserRepoCount(user.login).body()
                    }
                }
            }

            val repoMap = repoCountsDeferred.awaitAll()
                .filterNotNull()
                .associateBy { it.login }

            val userinfo = searchUserList.mapNotNull { user ->
                repoMap[user.login]?.let { repo ->
                    UserInfo(
                        login = user.login,
                        id = user.id,
                        avatarUrl = user.avatarUrl,
                        htmlUrl = user.htmlUrl,
                        publicRepoCount = repo.publicRepoCount
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

