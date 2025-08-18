package com.hater.githubsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
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
import com.jakewharton.rxbinding4.widget.textChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
    private lateinit var searchEditTextSubscription: Disposable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initSearchUserEditText()
        initRecyclerView()
        initObserve()
        initLoadState()
    }

    private fun initSearchUserEditText() {
        val editTextChangeObservable = binding.searchEditText.textChanges()
        searchEditTextSubscription =
            editTextChangeObservable
                .debounce(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    val query = it.trim().toString()
                    if (query.isNotEmpty()) {
                        callSearch(query)
                    }
                }
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

    override fun onDestroy() {
        super.onDestroy()
        searchEditTextSubscription.dispose()
    }
}
