package com.hater.githubsearch.api

import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.model.GithubUserRepo
import com.hater.githubsearch.model.GithubUserResponse
import org.json.JSONObject

object GithubApiParser {

    fun parseGithubUserRepo(jsonString: String?): GithubUserRepo? {
        return jsonString?.takeIf { it.isNotEmpty() }?.let {
            val jsonObject = JSONObject(jsonString)
            val login = jsonObject.getString("login")
            val publicRepoCount = jsonObject.getInt("public_repos")

            GithubUserRepo(
                login,
                publicRepoCount
            )
        }
    }

    fun parseGithubUserSearchResponse(jsonString: String?): GithubUserResponse? {
        return jsonString?.takeIf { it.isNotEmpty() }?.let {
            val jsonObject = JSONObject(jsonString)
            val itemsArray = jsonObject.getJSONArray("items")
            val userList = mutableListOf<GithubUser>()

            for (i in 0 until itemsArray.length()) {
                val userObject = itemsArray.getJSONObject(i)
                val login = userObject.getString("login")
                val id = userObject.getInt("id")
                val avatarUrl = userObject.getString("avatar_url")
                val htmlUrl = userObject.getString("html_url")

                userList.add(
                    GithubUser(
                        login = login,
                        id = id,
                        avatarUrl = avatarUrl,
                        htmlUrl = htmlUrl
                    )
                )
            }
            return GithubUserResponse(userList)
        }
    }

}