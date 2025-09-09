package com.hater.githubsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
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
        initSearchUserEditText()
        initRecyclerView()
        initObserve()
        initLoadState()
    }

    private fun initSearchUserEditText() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchViewModel.updateSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }



    private fun callSearch(query: String) {
        searchViewModel.searchUser(query)
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

