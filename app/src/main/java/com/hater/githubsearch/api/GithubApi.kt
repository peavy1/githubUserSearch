package com.hater.githubsearch.api

import android.util.Log
import com.google.gson.Gson
import com.hater.githubsearch.BuildConfig
import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.GithubUserResponse
import com.hater.githubsearch.util.Constants
import com.hater.githubsearch.util.Constants.TIME_OUT_MILLIS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GithubApi {

    private fun createConnection(
        method: String,
        endpoint: String,
        queryParams: Map<String, Any> = emptyMap()
    ): HttpURLConnection {
        val queryString = if (queryParams.isNotEmpty()) {
            queryParams.map { (key, value) ->
                val encodedValue = URLEncoder.encode(value.toString(), "UTF-8")
                "$key=$encodedValue"
            }.joinToString("&", prefix = "?")
        } else ""

        val url = URL(Constants.BASE_URL + endpoint + queryString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = TIME_OUT_MILLIS
        connection.readTimeout = TIME_OUT_MILLIS
        connection.setRequestProperty(Constants.AUTHORIZATION, "${Constants.BEARER} ${BuildConfig.githubApiKey}")
        return connection
    }

    suspend fun searchUser(searchKeyword: String, page: Int): GithubUserResponse? {
        return withContext(Dispatchers.IO) {
            val endpoint = "search/users"
            val queryParams = mapOf(
                "q" to searchKeyword,
                "page" to page
            )

            var connection: HttpURLConnection? = null
            try {
                connection = createConnection("GET", endpoint, queryParams)
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = InputStreamReader(connection.inputStream, "UTF-8")
                    val responseText = BufferedReader(reader).use { it.readText() }
                    Gson().fromJson(responseText, GithubUserResponse::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        }
    }

    suspend fun getUserRepoCount(username: String): GithubUserRepo? {
        return withContext(Dispatchers.IO) {
            val endpoint = "users/$username"
            var connection: HttpURLConnection? = null
            try {
                connection = createConnection("GET", endpoint)
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = InputStreamReader(connection.inputStream, "UTF-8")
                    val responseText = BufferedReader(reader).use { it.readText() }
                    Gson().fromJson(responseText, GithubUserRepo::class.java)
                } else {
                    println("Error: ${connection.responseMessage}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        }

    }
}


