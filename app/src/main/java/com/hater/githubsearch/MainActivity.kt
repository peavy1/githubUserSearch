package com.hater.githubsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.hater.githubsearch.databinding.ActivityMainBinding
import com.hater.githubsearch.ui.adapter.SearchUserAdapter
import com.hater.githubsearch.ui.adapter.SearchUserLoadStateAdapter
import com.hater.githubsearch.viewmodel.GithubSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_USER_URL = "user_url"
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val searchViewModel: GithubSearchViewModel by viewModels()
    private lateinit var searchUserAdapter: SearchUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initSearchButton()
        initRecyclerView()
        initObserve()
        initLoadState()
    }

    private fun initSearchButton() {
        binding.btnSearch.setOnClickListener {
            callSearch()
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                callSearch()
            }
            true
        }
    }

    private fun callSearch() {
        val search = binding.searchEditText.text?.trim().toString()
        searchViewModel.searchUser(search)
    }

    private fun initRecyclerView() {
        searchUserAdapter = SearchUserAdapter()
        binding.recyclerView.apply {
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.VERTICAL
                )
            )

            adapter = searchUserAdapter.withLoadStateFooter(
                footer = SearchUserLoadStateAdapter(searchUserAdapter::retry)
            )
        }
    }

    private fun initObserve() {
        lifecycleScope.launch {
            searchViewModel.searchPagingResult.collectLatest {
                searchUserAdapter.submitData(it)
            }
        }
    }

    private fun initLoadState() {
        searchUserAdapter.addLoadStateListener { combinedLoadStates ->
            val loadState = combinedLoadStates.source
            val isListEmpty = searchUserAdapter.itemCount < 1
                    && loadState.refresh is LoadState.NotLoading
                    && loadState.append.endOfPaginationReached


            binding.emptyTextview.isVisible = isListEmpty
            binding.recyclerView.isVisible = !isListEmpty

            binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
        }
    }
}
