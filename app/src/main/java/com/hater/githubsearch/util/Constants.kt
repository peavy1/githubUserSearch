package com.hater.githubsearch.util

import com.hater.githubsearch.BuildConfig

object Constants {
    const val BASE_URL = "https://api.github.com/"
    const val PAGING_SIZE = 30
    const val API_KEY = BuildConfig.githubApiKey
    const val BEARER = "Bearer"
    const val USER_NAME_QUALIFIER = "+in:login"
}