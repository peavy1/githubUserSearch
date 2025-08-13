package com.hater.githubsearch.api

import com.hater.githubsearch.model.GithubUserResponse
import com.hater.githubsearch.util.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubSearchApi {

    @GET("/search/users")
    suspend fun searchUser(
        @Query("q", encoded = true) searchKeyword: String,
        @Query("page") page:Int
    ): Response<GithubUserResponse>

}

