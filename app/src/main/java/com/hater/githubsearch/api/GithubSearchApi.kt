package com.hater.githubsearch.api

import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.GithubUserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubSearchApi {

    @GET("/search/users")
    suspend fun searchUser(
        @Query("q", encoded = true) searchKeyword: String,
        @Query("page") page:Int
    ): Response<GithubUserResponse>

    @GET("users/{username}")
    suspend fun getUserRepoCount(
        @Path("username") username: String
    ): Response<GithubUserRepo>
}

