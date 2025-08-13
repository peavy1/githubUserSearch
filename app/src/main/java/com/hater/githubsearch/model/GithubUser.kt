package com.hater.githubsearch.model

import com.google.gson.annotations.SerializedName

data class GithubUser(

    val login:String,
    val id:Int,
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String
)