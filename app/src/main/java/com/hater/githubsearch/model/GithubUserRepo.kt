package com.hater.githubsearch.model

import com.google.gson.annotations.SerializedName

data class GithubUserRepo(
    val login:String,
    @SerializedName("public_repos")
    val publicRepoCount: Int
)