package com.hater.githubsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hater.githubsearch.model.GithubUser
import com.hater.githubsearch.repository.SearchPagingSource
import com.hater.githubsearch.repository.SearchRepository
import com.hater.githubsearch.util.Constants.PAGING_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GithubSearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
): ViewModel() {

    private val _searchPagingResult = MutableStateFlow<PagingData<GithubUser>>(PagingData.empty())
    val searchPagingResult: StateFlow<PagingData<GithubUser>> = _searchPagingResult.asStateFlow()

    fun searchUser(query: String) {
        viewModelScope.launch {
            searchRepository.searchBookPaging(query)
                .cachedIn(viewModelScope)
                .collectLatest {
                    _searchPagingResult.value = it
                }
        }
    }


}