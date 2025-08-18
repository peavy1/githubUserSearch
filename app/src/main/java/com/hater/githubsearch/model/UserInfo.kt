package com.hater.githubsearch.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    val login:String,
    val id:Int,
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("public_repos")
    val publicRepoCount: Int

)