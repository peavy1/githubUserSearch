package com.hater.githubsearch.model

import androidx.resourceinspection.annotation.Attribute.IntMap
import com.google.gson.annotations.SerializedName

data class GithubUserResponse (
    @SerializedName("total_count")
    val totalCount: Int,
    val items:List<GithubUser>
)