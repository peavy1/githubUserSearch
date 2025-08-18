package com.hater.githubsearch.model

import androidx.resourceinspection.annotation.Attribute.IntMap
import com.google.gson.annotations.SerializedName

data class GithubUserResponse (
    val items:List<GithubUser>
)