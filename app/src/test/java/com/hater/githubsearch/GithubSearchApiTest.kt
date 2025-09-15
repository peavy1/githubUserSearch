package com.hater.githubsearch

import android.util.Log
import com.hater.githubsearch.api.GithubSearchApi
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.GithubUserResponse
import com.hater.githubsearch.model.UserInfo
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GithubSearchApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: GithubSearchApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GithubSearchApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun getJson(path: String): String {
        return javaClass.classLoader!!.getResource(path)!!.readText()
    }

    @Test
    fun searchUserResponseJsonSyntaxException() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(getJson("github_parse_error_response.json"))
        server.enqueue(mockResponse)

        assertFailsWith<JsonSyntaxException> {
            api.searchUser("query", 1)
        }
    }


    @Test
    fun searchUserResponseEmpty() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(getJson("search_result_empty.json"))
        server.enqueue(mockResponse)

        val response = api.searchUser("", 1)

        assertThat(response.isSuccessful).isTrue()
        assertThat(response.body()).isNotNull()
        assertThat(response.body()?.items).isEmpty()
    }

    @Test
    fun searchUserResponseRequestPath() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(getJson("search_result_empty.json"))
        server.enqueue(mockResponse)

        api.searchUser("test", 1)

        val request = server.takeRequest()

        assertThat(request.path).isEqualTo("/search/users?q=test&page=1")
    }

    @Test
    fun searchUserResponseGithubUserResponse() = runTest {
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(getJson("github_success_response.json"))
        server.enqueue(mockResponse)

        val response = api.searchUser("jake", 1)

        assertThat(response.isSuccessful).isTrue()

        val responseBody = response.body()
        assertThat(responseBody).isNotNull()

        val nonNullBody = responseBody as GithubUserResponse
        assertThat(nonNullBody.items).hasSize(1)

        val firstUser = nonNullBody.items[0]
        assertThat(firstUser).isNotNull()
        assertThat(firstUser.login).isEqualTo("jake")
        assertThat(firstUser.id).isEqualTo(1234)
    }


    @Test
    fun searchRepoAsync() = runTest {
        val searchResponseJson = getJson("search_users_success.json")
        val userList = Gson().fromJson(searchResponseJson, GithubUserResponse::class.java).items

        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(getJson("user_repo_success.json"))

        server.enqueue(mockResponse)
        server.enqueue(mockResponse)


        val responses = coroutineScope {
            val deferreds = userList.map { user ->
                async { api.getUserRepoCount(user.login) }
            }
            deferreds.awaitAll()
        }

        assertThat(responses).hasSize(2)
        assertThat(responses[0].body()?.publicRepoCount).isEqualTo(15)
        assertThat(responses[1].body()?.publicRepoCount).isEqualTo(15)
    }


    @Test
    fun searchRepoAsyncUserInfo() = runTest {
        val searchResponseJson = getJson("search_users_success.json")
        val userList = Gson().fromJson(searchResponseJson, GithubUserResponse::class.java).items

        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(getJson("user_a_repos.json"))
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(getJson("user_b_repos.json"))
        )

        val repoCountList = coroutineScope {
            val deferreds = userList.map { user ->
                async { api.getUserRepoCount(user.login).body() }
            }
            deferreds.awaitAll().filterNotNull()
        }

        val finalUserInfoList = mutableListOf<UserInfo>()
        userList.map { user ->
            repoCountList.firstOrNull { it.login == user.login }?.let { findUserRepo ->
                finalUserInfoList.add(
                    UserInfo(
                        login = user.login,
                        id = user.id,
                        avatarUrl = user.avatarUrl,
                        htmlUrl = user.htmlUrl,
                        publicRepoCount = findUserRepo.publicRepoCount
                    )
                )
            }
        }


        assertThat(finalUserInfoList).hasSize(2)

        val userAInfo = finalUserInfoList.find { it.login == "UserA" }
        assertThat(userAInfo).isNotNull()
        assertThat(userAInfo?.id).isEqualTo(101)
        assertThat(userAInfo?.publicRepoCount).isEqualTo(10)


        val userBInfo = finalUserInfoList.find { it.login == "UserB" }
        assertThat(userBInfo).isNotNull()
        assertThat(userBInfo?.id).isEqualTo(102)
        assertThat(userBInfo?.publicRepoCount).isEqualTo(25)
    }

    @Test
    fun searchUserRepoAsync() = runTest {
        val userList = Gson().fromJson(getJson("search_users_success.json"), GithubUserResponse::class.java).items
        server.enqueue(MockResponse().setBody(getJson("user_a_repos.json")))
        server.enqueue(MockResponse().setBody(getJson("user_b_repos.json")))

        val repoCountList = coroutineScope {
            val deferred = userList.map { user ->
                async { api.getUserRepoCount(user.login).body() }
            }
            deferred.awaitAll().filterNotNull()
        }

        assertThat(repoCountList).hasSize(2)
        assertThat(repoCountList.find { it.login == "UserA" }?.publicRepoCount).isEqualTo(10)
        assertThat(repoCountList.find { it.login == "UserB" }?.publicRepoCount).isEqualTo(25)
    }

    @Test
    fun searchUserRepoCombine() = runTest {
        val userList = Gson().fromJson(getJson("search_users_success.json"), GithubUserResponse::class.java).items
        val listType = object : TypeToken<List<GithubUserRepo>>() {}.type
        val repoCountList:List<GithubUserRepo>  = Gson().fromJson(getJson("repo_list.json"), listType)

        val finalUserInfoList = combineUserData(userList, repoCountList)

        assertThat(finalUserInfoList).hasSize(2)

        val userAInfo = finalUserInfoList.find { it.login == "UserA" }
        assertThat(userAInfo).isNotNull()
        assertThat(userAInfo?.id).isEqualTo(101)
        assertThat(userAInfo?.publicRepoCount).isEqualTo(10)


        val userBInfo = finalUserInfoList.find { it.login == "UserB" }
        assertThat(userBInfo).isNotNull()
        assertThat(userBInfo?.id).isEqualTo(102)
        assertThat(userBInfo?.publicRepoCount).isEqualTo(25)
    }


    private fun combineUserData(
        users: List<GithubUser>,
        repos: List<GithubUserRepo>
    ): List<UserInfo> {
        return users.mapNotNull { user ->
            repos.firstOrNull { it.login == user.login }?.let { repoInfo ->
                UserInfo(
                    login = user.login,
                    id = user.id,
                    avatarUrl = user.avatarUrl,
                    htmlUrl = user.htmlUrl,
                    publicRepoCount = repoInfo.publicRepoCount
                )
            }
        }
    }

}