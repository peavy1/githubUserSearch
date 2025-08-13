package com.hater.githubsearch.repository


import com.hater.githubsearch.model.GithubUser

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hater.githubsearch.api.GithubSearchApi
import com.hater.githubsearch.util.Constants.USER_NAME_QUALIFIER

import retrofit2.HttpException
import java.io.IOException

class SearchPagingSource(
    private val api: GithubSearchApi,
    private val query: String
) : PagingSource<Int, GithubUser>() {

    override fun getRefreshKey(state: PagingState<Int, GithubUser>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GithubUser> {
        return try {
            val pageNumber = params.key ?: STARTING_PAGE_INDEX
            val result = api.searchUser("$query$USER_NAME_QUALIFIER", pageNumber)
            val data = result.body()?.items ?: emptyList()
            val prevKey = if (pageNumber == STARTING_PAGE_INDEX) {
                null
            } else {
                pageNumber - 1
            }
            val nextKey = if (data.isEmpty()) {
                null
            } else {
                pageNumber + 1
            }

            LoadResult.Page(
                data = data,
                prevKey = prevKey,
                nextKey = nextKey,
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }


    companion object {
        const val STARTING_PAGE_INDEX = 1
    }
}