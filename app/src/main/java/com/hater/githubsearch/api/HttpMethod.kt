package com.hater.githubsearch.api

sealed class HttpMethod(val method:String) {
    data object GET:HttpMethod("GET")
    data object POST:HttpMethod("POST")

}