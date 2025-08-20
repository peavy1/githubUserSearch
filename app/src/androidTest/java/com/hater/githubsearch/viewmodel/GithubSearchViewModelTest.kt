package com.hater.githubsearch.viewmodel

import android.util.Log
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.testing.asSnapshot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.hater.githubsearch.model.UserInfo
import com.hater.githubsearch.repository.FakeSearchRepository
import com.hater.githubsearch.repository.SearchRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GithubSearchViewModelTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: SearchRepository

    private lateinit var viewModel: GithubSearchViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        hiltRule.inject()
        Dispatchers.setMain(testDispatcher)
        viewModel = GithubSearchViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun Viewmodel에서_searchUser_호출_시_데이터가_업데이트되는가() = runTest {
        val expectedUsers = listOf(
            UserInfo("user1", 1, "url1", "html_url1", 10),
            UserInfo("user2", 2, "url2", "html_url2", 25),
            UserInfo("user3", 3, "url3", "html_url3", 44),
            UserInfo("user4", 4, "url4", "html_url4", 125),
            UserInfo("user5", 5, "url2", "html_url5", 3)
        )

        viewModel.searchUser("test")

        val fakePagingData = PagingData.from(expectedUsers)
        (repository as FakeSearchRepository).setNextPagingData(fakePagingData)

        val actualPagingData = viewModel.searchPagingResult.first()

        val actualUsers = flowOf(actualPagingData).asSnapshot()
        assertThat(actualUsers).isEqualTo(expectedUsers)
    }

    @Test
    fun ViewModel이_생성되고_초기_데이터는_비어있는가() = runTest {
        val initialData: PagingData<UserInfo> = viewModel.searchPagingResult.first()
        val snapshot = flowOf(initialData).asSnapshot()
        assertThat(snapshot).isEmpty()
    }


    @Test
    fun 검색어가_변경되면_새로운_결과를_가져오는가() = runTest {
        val repository = repository as FakeSearchRepository
        val query1 = "search1"
        val query2 = "search2"

        val result1 = listOf(UserInfo("user1", 1, "", "", 10))
        val result2 = listOf(UserInfo("user2", 2, "", "", 20))

        viewModel.searchUser(query1)
        repository.setNextPagingData(PagingData.from(result1))

        var snapshot = flowOf(viewModel.searchPagingResult.first()).asSnapshot()
        assertThat(snapshot).isEqualTo(result1)

        viewModel.searchUser(query2)
        repository.setNextPagingData(PagingData.from(result2))

        snapshot = flowOf(viewModel.searchPagingResult.first()).asSnapshot()
        assertThat(snapshot).isEqualTo(result2)
    }

}