package com.hater.githubsearch.api

import android.util.Log
import com.google.gson.Gson
import com.hater.githubsearch.BuildConfig
import com.hater.githubsearch.api.GithubApiParser.parseGithubUserRepo
import com.hater.githubsearch.api.GithubApiParser.parseGithubUserSearchResponse
import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.GithubUserResponse
import com.hater.githubsearch.util.Constants
import com.hater.githubsearch.util.Constants.TIME_OUT_MILLIS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GithubApi {

    private suspend fun <T : Any> makeRequest(
        method: HttpMethod,
        path: String,
        queryParams: Map<String, Any> = emptyMap(),
        classOfT: Class<T>
    ): T? {
        val (urlString, connection) = createConnection(method, path, queryParams)
        HttpLogger.logRequest(method.method, urlString)

        return withContext(Dispatchers.IO) {

            try {
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    HttpLogger.logSuccess(responseCode, urlString)

                    val reader = InputStreamReader(connection.inputStream, "UTF-8")
                    val responseText = BufferedReader(reader).use { it.readText() }
                    Gson().fromJson(responseText, classOfT)
                } else {
                    HttpLogger.logError(responseCode, urlString)
                    null
                }
            } catch (e: Exception) {
                HttpLogger.logFailure(e)
                e.printStackTrace()
                null
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun createConnection(
        httpMethod: HttpMethod,
        path: String,
        queryParams: Map<String, Any> = emptyMap()
    ): Pair<String, HttpURLConnection> {
        val queryString = if (queryParams.isNotEmpty()) {
            queryParams.map { (key, value) ->
                val encodedValue = URLEncoder.encode(value.toString(), "UTF-8")
                "$key=$encodedValue"
            }.joinToString("&", prefix = "?")
        } else ""

        val url = URL(Constants.BASE_URL + path + queryString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = httpMethod.method
        connection.connectTimeout = TIME_OUT_MILLIS
        connection.readTimeout = TIME_OUT_MILLIS
        connection.setRequestProperty(Constants.AUTHORIZATION, "${Constants.BEARER} ${BuildConfig.githubApiKey}")
        return Pair(url.toString(), connection)
    }

    suspend fun searchUser(searchKeyword: String, page: Int): GithubUserResponse? {
        return makeRequest(
            method = HttpMethod.GET,
            path = "search/users",
            queryParams = mapOf("q" to searchKeyword, "page" to page),
            classOfT = GithubUserResponse::class.java
        )
    }

    suspend fun getUserRepoCount(username: String): GithubUserRepo? {
        return makeRequest(
            method = HttpMethod.GET,
            path = "users/$username",
            classOfT = GithubUserRepo::class.java
        )
    }

}



