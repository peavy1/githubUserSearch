package com.hater.githubsearch.api

import com.hater.githubsearch.util.Constants
import com.hater.githubsearch.util.Constants.API_KEY
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "${Constants.BEARER} $API_KEY")
            .build()
        return chain.proceed(request)
    }
}
